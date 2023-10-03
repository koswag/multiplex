package pl.kskarzynski.multiplex.domain.services.pricing

import arrow.core.getOrElse
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import pl.kskarzynski.multiplex.domain.model.booking.Booking
import pl.kskarzynski.multiplex.domain.model.booking.ValidBooking
import pl.kskarzynski.multiplex.domain.model.screening.*
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket
import pl.kskarzynski.multiplex.domain.model.ticket.TicketType
import pl.kskarzynski.multiplex.domain.model.user.UserInfo
import java.time.LocalDateTime

private object TestBookingPricingPolicy : BookingPricingPolicy

class BookingPricingPolicySpec : FeatureSpec({

    feature("Booking pricing policy") {
        scenario("Regular booking with various tickets") {
            checkAll(
                Arb.list(
                    gen = Arb.enum<TicketType>(),
                    range = 1..20,
                )
            ) { ticketTypes ->
                // given:
                val booking = bookingOf(ticketTypes = ticketTypes)

                // when:
                val price = TestBookingPricingPolicy(booking)

                // then:
                price.value shouldBe
                    ticketTypes.sumOf { it.price.value }
            }
        }
    }

})

private fun bookingOf(ticketTypes: Collection<TicketType>): ValidBooking =
    Booking(
        userInfo = UserInfo("Andrzej", "Kowalski")
            .getOrElse { throw IllegalArgumentException(it.joinToString()) },
        tickets = ticketTypes
            .mapIndexed { i, type ->
                Ticket(
                    type = type,
                    seatPlacement = SeatPlacement(SeatRow(1), SeatNumber(i)),
                )
            },
        screening = ScreeningInfo(
            id = ScreeningId.generate(),
            title = MovieTitle("Movie Title"),
            startTime = LocalDateTime.of(2023, 10, 3, 16, 30),
            room = Room(RoomNumber(1), seats = emptyList()),
        )
    ).let(::ValidBooking)
