package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.next
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds
import org.jetbrains.exposed.sql.transactions.transaction
import pl.kskarzynski.multiplex.common.test.arbs.bookingId
import pl.kskarzynski.multiplex.common.test.arbs.screeningId
import pl.kskarzynski.multiplex.common.test.exposed.initializeDatabase
import pl.kskarzynski.multiplex.common.test.testcontainers.installPostgresContainer
import pl.kskarzynski.multiplex.common.utils.datetime.plus
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.ConfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.ExpiredBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.screenings.infra.util.booking
import pl.kskarzynski.multiplex.screenings.infra.util.confirmedBooking
import pl.kskarzynski.multiplex.screenings.infra.util.expiredBooking
import pl.kskarzynski.multiplex.screenings.infra.util.unconfirmedBooking
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class BookingTableTest : FeatureSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    beforeSpec {
        val dataSource = installPostgresContainer()
        initializeDatabase(dataSource, BookingTable)
    }

    feature("Finding Bookings by Screening") {
        scenario("Table is empty") {
            // given:
            val screeningId = Arb.screeningId().next()

            // when:
            val result = transaction { BookingTable.findBookings(screeningId) }

            // then:
            expectThat(result).isEmpty()
        }

        scenario("Bookings exist") {
            // given:
            val screeningId = Arb.screeningId().next()
            val otherScreeningId = Arb.screeningId().next()

            val screeningBookings = List(3) { Arb.booking().next() }
            BookingTable.saveAll(screeningId, screeningBookings)

            val otherScreeningBookings = List(3) { Arb.booking().next() }
            BookingTable.saveAll(otherScreeningId, otherScreeningBookings)

            // when:
            val result = transaction { BookingTable.findBookings(screeningId) }

            // then:
            expectThat(result) containsExactlyInAnyOrder screeningBookings
        }
    }

    feature("Saving a Booking") {
        scenario("Table is empty") {
            // given:
            val screeningId = Arb.screeningId().next()
            val booking = Arb.booking().next()

            // when:
            transaction {
                BookingTable.save(screeningId, booking)
            }

            // then:
            val screeningBookings = transaction { BookingTable.findBookings(screeningId) }
            expectThat(screeningBookings).hasSize(1) and {
                get { first() } isEqualTo booking
            }
        }

        scenario("Other bookings exist") {
            // given:
            val screeningId = Arb.screeningId().next()
            val booking = Arb.booking().next()

            val otherScreeningId = Arb.screeningId().next()
            val otherScreeningBookings = List(3) { Arb.booking().next() }
            BookingTable.saveAll(otherScreeningId, otherScreeningBookings)

            // when:
            transaction {
                BookingTable.save(screeningId, booking)
            }

            // then:
            val screeningBookings = transaction { BookingTable.findBookings(screeningId) }
            expectThat(screeningBookings).containsExactly(booking)

            val otherBookingScreenings = transaction { BookingTable.findBookings(otherScreeningId) }
            expectThat(otherBookingScreenings) containsExactlyInAnyOrder otherBookingScreenings
        }
    }

    feature("Finding expired Booking Screening IDs") {
        scenario("Table is empty") {
            // given:
            val currentTime = Arb.localDateTime().next()

            // when:
            val result = transaction { BookingTable.findExpiredBookingScreeningIds(currentTime) }

            //then:
            expectThat(result).isEmpty()
        }

        scenario("There are no expired bookings") {
            // given:
            val currentTime = Arb.localDateTime().next()

            val screeningId = Arb.screeningId().next()
            val nonExpiredBookings = List(5) { Arb.nonExpiredUnconfirmedBooking(currentTime).next() }
            BookingTable.saveAll(screeningId, nonExpiredBookings)

            // when:
            val screeningIds = transaction { BookingTable.findExpiredBookingScreeningIds(currentTime) }

            // then:
            expectThat(screeningIds).isEmpty()
        }

        scenario("There are expired bookings") {
            // given:
            val currentTime = Arb.localDateTime().next()

            val screeningId = Arb.screeningId().next()
            val nonExpiredBookings = List(5) { Arb.nonExpiredUnconfirmedBooking(currentTime).next() }
            val expiredBookings = List(5) { Arb.expiredUnconfirmedBooking(currentTime).next() }
            BookingTable.saveAll(screeningId, nonExpiredBookings + expiredBookings)

            val otherScreeningId = Arb.screeningId().next()
            val otherNonExpiredBookings = List(5) { Arb.nonExpiredUnconfirmedBooking(currentTime).next() }
            val expiredBooking = Arb.expiredBooking(maxExpirationTime = currentTime).next()
            val confirmedBooking = Arb.confirmedBooking(maxExpirationTime = currentTime).next()
            BookingTable.saveAll(otherScreeningId, otherNonExpiredBookings + expiredBooking + confirmedBooking)

            // when:
            val result = transaction { BookingTable.findExpiredBookingScreeningIds(currentTime) }

            // then:
            expectThat(result).containsExactly(screeningId)
        }
    }

    feature("Finding Screening ID by booking") {
        scenario("Table is empty") {
            // given:
            val bookingId = Arb.bookingId().next()

            // when:
            val result = transaction { BookingTable.findScreeningIdByBooking(bookingId) }

            // then:
            expectThat(result).isNull()
        }

        scenario("Other booking exists") {
            // given:
            val screeningId = Arb.screeningId().next()
            val existentBooking = Arb.booking().next()
            transaction { BookingTable.save(screeningId, existentBooking) }

            val nonExistentBookingId = Arb.bookingId().next()

            // when:
            val result = transaction { BookingTable.findScreeningIdByBooking(nonExistentBookingId) }

            // then:
            expectThat(result).isNull()
        }

        scenario("Screening has bookings") {
            // given:
            val screeningId = Arb.screeningId().next()
            val booking = Arb.booking().next()
            transaction { BookingTable.save(screeningId, booking) }

            // when:
            val result = transaction { BookingTable.findScreeningIdByBooking(booking.id) }

            // then:
            expectThat(result) isEqualTo screeningId
        }
    }

})

private fun BookingTable.saveAll(screeningId: ScreeningId, bookings: Iterable<Booking>) {
    transaction {
        for (booking in bookings) {
            save(screeningId, booking)
        }
    }
}

private fun Arb.Companion.nonExpiredUnconfirmedBooking(currentTime: LocalDateTime): Arb<UnconfirmedBooking> =
    Arb.unconfirmedBooking(
        expirationTime = Arb.localDateTimeAfter(currentTime),
    )

private fun Arb.Companion.expiredUnconfirmedBooking(currentTime: LocalDateTime): Arb<UnconfirmedBooking> =
    Arb.unconfirmedBooking(
        expirationTime = Arb.localDateTime(maxLocalDateTime = currentTime),
    )

private fun Arb.Companion.localDateTimeAfter(localDateTime: LocalDateTime): Arb<LocalDateTime> =
    Arb.localDateTime(minLocalDateTime = localDateTime + 1.seconds)

private fun Arb.Companion.expiredBooking(maxExpirationTime: LocalDateTime): Arb<ExpiredBooking> =
    Arb.expiredBooking(
        expirationTime = Arb.localDateTime(maxLocalDateTime = maxExpirationTime),
    )

private fun Arb.Companion.confirmedBooking(maxExpirationTime: LocalDateTime): Arb<ConfirmedBooking> =
    Arb.confirmedBooking(
        expirationTime = Arb.localDateTime(maxLocalDateTime = maxExpirationTime),
    )
