package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table

import arrow.core.toNonEmptyListOrNull
import java.time.LocalDateTime
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import pl.kskarzynski.multiplex.common.infra.exposed.jsonb
import pl.kskarzynski.multiplex.common.infra.json.JSON
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.ConfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.ExpiredBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus.CONFIRMED
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus.EXPIRED
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus.UNCONFIRMED
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentTicket
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentUserInfo
import pl.kskarzynski.multiplex.shared.booking.BookingConfirmationTime
import pl.kskarzynski.multiplex.shared.booking.BookingExpirationTime
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.booking.BookingPrice
import pl.kskarzynski.multiplex.shared.booking.BookingTime
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

internal object BookingTable : UUIDTable("multiplex_screenings.bookings") {
    val screeningId = uuid("screening_id")
    val status = enumerationByName<PersistentBookingStatus>("status", length = 16)
    val userInfo = jsonb<PersistentUserInfo>("user_info", JSON.mapper)
    val tickets = jsonb<List<PersistentTicket>>("tickets", JSON.mapper)
    val bookingTime = datetime("booking_time")
    val totalPrice = decimal("total_price", 5, 2).nullable()
    val expirationTime = datetime( "expiration_time").nullable()
    val confirmationTime = datetime( "confirmation_time").nullable()

    fun findBookings(scrId: ScreeningId): List<Booking> =
        selectAll()
            .where { screeningId eq scrId.value }
            .map { rowToDomain(it) }

    private fun rowToDomain(row: ResultRow): Booking =
        when (row[status]) {
            UNCONFIRMED -> rowToUnconfirmedBooking(row)
            CONFIRMED -> rowToConfirmedBooking(row)
            EXPIRED -> rowToExpiredBooking(row)
        }

    fun save(scrId: ScreeningId, booking: Booking) {
        insertIgnore {
            it[id] = booking.id.value
            it[screeningId] = scrId.value
            it[userInfo] = PersistentUserInfo.from(booking.userInfo)
            it[tickets] = booking.tickets.map { PersistentTicket.from(it) }
            it[bookingTime] = booking.bookingTime.value

            it[status] = booking.status
            it[totalPrice] = booking.totalPrice?.value
            it[expirationTime] = booking.expirationTime.value
            it[confirmationTime] = if (booking is ConfirmedBooking) booking.confirmationTime.value else null
        }
    }

    fun findExpiredBookingScreeningIds(currentTime: LocalDateTime): List<ScreeningId> =
        select(screeningId)
            .withDistinct(true)
            .where { (status eq UNCONFIRMED) and (expirationTime lessEq currentTime) }
            .map { row -> ScreeningId(row[screeningId]) }

    fun findScreeningIdByBooking(bookingId: BookingId): ScreeningId? =
        select(screeningId)
            .where { id eq bookingId.value }
            .firstOrNull()
            ?.let { row -> ScreeningId(row[screeningId]) }
}

private fun BookingTable.rowToUnconfirmedBooking(row: ResultRow): UnconfirmedBooking {
    val id = BookingId(row[id].value)
    val price = row[totalPrice] ?: error("Unconfirmed booking has no price")
    val expiration = row[expirationTime] ?: error("Unconfirmed booking has no expiration time")
    val tickets = row[tickets].map { it.toDomain() }.toNonEmptyListOrNull()
        ?: error("Unconfirmed booking of ID $id has no tickets")

    return UnconfirmedBooking(
        id = id,
        userInfo = row[userInfo].toDomain(),
        tickets = tickets,
        bookingTime = BookingTime(row[bookingTime]),
        totalPrice = BookingPrice(price),
        expirationTime = BookingExpirationTime(expiration),
    )
}

private fun BookingTable.rowToConfirmedBooking(row: ResultRow): ConfirmedBooking {
    val price = row[totalPrice] ?: error("Confirmed booking has no price")
    val expiration = row[expirationTime] ?: error("Confirmed booking has no expiration time")
    val confirmation = row[confirmationTime] ?: error("Confirmed booking has no confirmation time")
    val tickets = row[tickets].map { it.toDomain() }.toNonEmptyListOrNull()
        ?: error("Confirmed booking of ID $id has no tickets")

    return ConfirmedBooking(
        id = BookingId(row[id].value),
        userInfo = row[userInfo].toDomain(),
        tickets = tickets,
        bookingTime = BookingTime(row[bookingTime]),
        totalPrice = BookingPrice(price),
        expirationTime = BookingExpirationTime(expiration),
        confirmationTime = BookingConfirmationTime(confirmation),
    )
}

private fun BookingTable.rowToExpiredBooking(row: ResultRow): ExpiredBooking {
    val expiration = row[expirationTime] ?: error("Unconfirmed booking has no expiration time")
    val tickets = row[tickets].map { it.toDomain() }.toNonEmptyListOrNull()
        ?: error("Expired booking of ID $id has no tickets")

    return ExpiredBooking(
        id = BookingId(row[id].value),
        userInfo = row[userInfo].toDomain(),
        tickets = tickets,
        bookingTime = BookingTime(row[bookingTime]),
        expirationTime = BookingExpirationTime(expiration),
    )
}

private val Booking.status: PersistentBookingStatus
    get() = when (this) {
        is UnconfirmedBooking -> UNCONFIRMED
        is ExpiredBooking -> EXPIRED
        is ConfirmedBooking -> CONFIRMED
    }

private val Booking.totalPrice: BookingPrice?
    get() = when (this) {
        is UnconfirmedBooking -> totalPrice
        is ConfirmedBooking -> totalPrice
        is ExpiredBooking -> null
    }
