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
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBooking
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus.UNCONFIRMED
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentTicket
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentUserInfo
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.toDomain
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.toPersistentBooking
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

internal object BookingTable : UUIDTable("multiplex_screenings.bookings") {
    val screeningId = uuid("screening_id")
    val status = enumerationByName<PersistentBookingStatus>("status", length = 16)
    val userInfo = jsonb<PersistentUserInfo>("user_info", JSON.mapper)
    val tickets = jsonb<List<PersistentTicket>>("tickets", JSON.mapper)
    val bookingTime = datetime("booking_time")
    val totalPrice = decimal("total_price", 5, 2).nullable()
    val expirationTime = datetime( "expiration_time")
    val confirmationTime = datetime( "confirmation_time").nullable()

    fun findBookings(scrId: ScreeningId): List<Booking> =
        selectAll()
            .where { screeningId eq scrId.value }
            .map { rowToPersistentBooking(it).toDomain() }

    fun findBookings(screeningIds: Collection<ScreeningId>): List<PersistentBooking> =
        selectAll()
            .where { screeningId inList screeningIds.map { it.value } }
            .map { rowToPersistentBooking(it) }

    fun save(booking: Booking, screeningId: ScreeningId) {
        save(booking.toPersistentBooking(screeningId))
    }

    private fun save(booking: PersistentBooking) {
        insertIgnore {
            it[id] = booking.id
            it[screeningId] = booking.screeningId
            it[userInfo] = booking.userInfo
            it[tickets] = booking.tickets
            it[bookingTime] = booking.bookingTime
            it[status] = booking.status
            it[totalPrice] = booking.totalPrice
            it[expirationTime] = booking.expirationTime
            it[confirmationTime] = booking.confirmationTime
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

    private fun rowToPersistentBooking(row: ResultRow) =
        PersistentBooking(
            id = row[id].value,
            status = row[status],
            screeningId = row[screeningId],
            userInfo = row[userInfo],
            tickets = row[tickets].toNonEmptyListOrNull() ?: error("Unconfirmed booking of ID $id has no tickets"),
            bookingTime = row[bookingTime],
            expirationTime = row[expirationTime],
            totalPrice = row[totalPrice],
            confirmationTime = row[confirmationTime],
        )
}
