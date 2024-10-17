package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table

import java.time.LocalDateTime
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.javatime.datetime
import pl.kskarzynski.multiplex.common.infra.exposed.jsonb
import pl.kskarzynski.multiplex.common.infra.json.JSON
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.ConfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.ExpiredBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.BookingStatus
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.BookingStatus.CONFIRMED
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.BookingStatus.EXPIRED
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.BookingStatus.UNCONFIRMED
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentTicket
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentUserInfo
import pl.kskarzynski.multiplex.shared.booking.BookingExpirationTime
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.booking.BookingPrice
import pl.kskarzynski.multiplex.shared.booking.BookingTime
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

object BookingTable : UUIDTable("multiplex_screenings.bookings") {
    val screeningId = reference("screening_id", ScreeningTable)
    val status = enumerationByName<BookingStatus>("status", length = 16)
    val userInfo = jsonb<PersistentUserInfo>("user_info", JSON.mapper)
    val tickets = jsonb<List<PersistentTicket>>("tickets", JSON.mapper)
    val bookingTime = datetime("booking_time")
    val totalPrice = decimal("total_price", 5, 2).nullable()
    val expirationTime = datetime( "expiration_time").nullable()

    fun findBookings(scrId: ScreeningId): List<Booking> =
        select(id, userInfo, status, tickets, bookingTime, totalPrice, expirationTime)
            .where { screeningId eq scrId.value }
            .map { rowToDomain(it) }

    fun rowToDomain(row: ResultRow) =
        when (row[status]) {
            UNCONFIRMED -> {
                val price = row[totalPrice] ?: error("Unconfirmed booking has no price")
                val expiration = row[expirationTime] ?: error("Unconfirmed booking has no expiration time")

                UnconfirmedBooking(
                    id = BookingId(row[id].value),
                    userInfo = row[userInfo].toDomain(),
                    tickets = row[tickets].map { it.toDomain() },
                    bookingTime = BookingTime(row[bookingTime]),
                    totalPrice = BookingPrice(price),
                    expirationTime = BookingExpirationTime(expiration),
                )
            }

            CONFIRMED -> {
                val price = row[totalPrice] ?: error("Unconfirmed booking has no price")

                ConfirmedBooking(
                    id = BookingId(row[id].value),
                    userInfo = row[userInfo].toDomain(),
                    tickets = row[tickets].map { it.toDomain() },
                    bookingTime = BookingTime(row[bookingTime]),
                    totalPrice = BookingPrice(price),
                )
            }

            EXPIRED -> {
                val expiration = row[expirationTime] ?: error("Unconfirmed booking has no expiration time")

                ExpiredBooking(
                    id = BookingId(row[id].value),
                    userInfo = row[userInfo].toDomain(),
                    tickets = row[tickets].map { it.toDomain() },
                    bookingTime = BookingTime(row[bookingTime]),
                    expirationTime = BookingExpirationTime(expiration),
                )
            }
        }

    fun saveBooking(screeningId: ScreeningId, booking: Booking) {
        insertIgnore {
            it[id] = screeningId.value
            it[userInfo] = PersistentUserInfo.from(booking.userInfo)
            it[tickets] = booking.tickets.map { PersistentTicket.from(it) }
            it[bookingTime] = booking.bookingTime.value

            it[status] = booking.status
            it[totalPrice] = booking.totalPrice?.value
            it[expirationTime] = booking.expirationTime?.value
        }
    }

    fun findExpiredBookingScreeningIds(currentTime: LocalDateTime): List<ScreeningId> =
        select(screeningId)
            .withDistinct(true)
            .where { (status eq UNCONFIRMED) and (expirationTime lessEq currentTime) }
            .map { row -> ScreeningId(row[screeningId].value) }

    fun findScreeningIdByBooking(bookingId: BookingId): ScreeningId? =
        select(screeningId)
            .where { id eq bookingId.value }
            .firstOrNull()
            ?.let { row -> ScreeningId(row[screeningId].value) }
}

private val Booking.status: BookingStatus
    get() =
        when (this) {
            is UnconfirmedBooking -> UNCONFIRMED
            is ExpiredBooking -> EXPIRED
            is ConfirmedBooking -> CONFIRMED
        }

private val Booking.totalPrice: BookingPrice?
    get() =
        when (this) {
            is UnconfirmedBooking -> totalPrice
            is ConfirmedBooking -> totalPrice
            is ExpiredBooking -> null
        }

private val Booking.expirationTime: BookingExpirationTime?
    get() =
        when (this) {
            is UnconfirmedBooking -> expirationTime
            is ExpiredBooking -> expirationTime
            is ConfirmedBooking -> null
        }
