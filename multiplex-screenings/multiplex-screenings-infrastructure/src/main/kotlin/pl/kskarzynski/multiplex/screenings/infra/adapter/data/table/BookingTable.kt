package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table

import arrow.core.toNonEmptyListOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.upsert
import pl.kskarzynski.multiplex.common.infra.exposed.jsonb
import pl.kskarzynski.multiplex.common.infra.json.JSON
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.*
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus.UNCONFIRMED
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object BookingTable : UuidTable("multiplex_screenings.bookings") {
    val screeningId = uuid("screening_id")
    val status = enumerationByName<PersistentBookingStatus>("status", length = 16)
    val userInfo = jsonb<PersistentUserInfo>("user_info", JSON.mapper)
    val tickets = jsonb<List<PersistentTicket>>("tickets", JSON.mapper)
    val bookingTime = datetime("booking_time")
    val totalPrice = decimal("total_price", 5, 2).nullable()
    val expirationTime = datetime( "expiration_time")
    val confirmationTime = datetime( "confirmation_time").nullable()

    fun findBookings(scrId: ScreeningId): Flow<Booking> =
        selectAll()
            .where { screeningId eq scrId.value }
            .map { rowToPersistentBooking(it).toDomain() }

    internal fun findBookings(screeningIds: Collection<ScreeningId>): Flow<PersistentBooking> =
        selectAll()
            .where { screeningId inList screeningIds.map { it.value } }
            .map { rowToPersistentBooking(it) }

    suspend fun save(booking: Booking, screeningId: ScreeningId) {
        save(booking.toPersistentBooking(screeningId))
    }

    private suspend fun save(booking: PersistentBooking) {
        upsert(id) {
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

    fun findExpiredBookingScreeningIds(currentTime: LocalDateTime): Flow<ScreeningId> =
        select(screeningId)
            .withDistinct(true)
            .where { (status eq UNCONFIRMED) and (expirationTime lessEq currentTime) }
            .map { row -> ScreeningId(row[screeningId]) }

    suspend fun findScreeningIdByBooking(bookingId: BookingId): ScreeningId? =
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
