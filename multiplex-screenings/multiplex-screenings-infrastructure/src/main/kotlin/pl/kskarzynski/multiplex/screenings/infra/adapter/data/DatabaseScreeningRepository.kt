@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.screenings.infra.adapter.data

import kotlinx.coroutines.flow.groupBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import pl.kskarzynski.multiplex.common.utils.datetime.currentTime
import pl.kskarzynski.multiplex.rooms.api.service.RoomService
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.model.request.MovieScreeningSearchRequest
import pl.kskarzynski.multiplex.screenings.domain.model.view.ScreeningListItemRoomView
import pl.kskarzynski.multiplex.screenings.domain.model.view.ScreeningListItemView
import pl.kskarzynski.multiplex.screenings.domain.port.data.ScreeningRepository
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.BookingTable
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.ScreeningTable
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.ScreeningData
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.toDomain
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.misc.Page
import pl.kskarzynski.multiplex.shared.misc.map
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import java.time.Clock
import kotlin.uuid.ExperimentalUuidApi

class DatabaseScreeningRepository(
    private val roomService: RoomService,
    private val clock: Clock,
) : ScreeningRepository {

    override suspend fun save(screening: Screening) {
        suspendTransaction {
            ScreeningTable.save(screening)

            for (booking in screening.bookings) {
                BookingTable.save(booking, screening.id)
            }
        }
    }

    override suspend fun findScreening(screeningId: ScreeningId): Screening? {
        val screening = suspendTransaction { ScreeningTable.find(screeningId) }
            ?: return null

        val room = roomService.findRoom(screening.roomId)
            ?: error("Room of ID ${screening.roomId.value} not found (screening: ${screeningId.value})")

        val bookings = suspendTransaction { BookingTable.findBookings(screening.id).toList() }
        return screening.toDomain(room, bookings)
    }

    override suspend fun findScreeningsByMovie(request: MovieScreeningSearchRequest): Page<ScreeningListItemView> =
        suspendTransaction {
            val screenings = ScreeningTable.findScreeningsByMovie(request)

            val roomIds = screenings.content.map { it.roomId }.toSet()
            val rooms = roomService.findRooms(roomIds).associateBy { it.id }

            screenings.map {
                val room = rooms[it.roomId] ?: error("Room ${it.roomId.value} not found (screening: ${it.id.value})")
                it.toListItem(room)
            }
        }

    override suspend fun findScreeningsWithExpiredBookings(): List<Screening> =
        suspendTransaction {
            val expiredBookingScreeningIds = BookingTable.findExpiredBookingScreeningIds(clock.currentTime()).toList()
            val screenings = ScreeningTable.findAll(expiredBookingScreeningIds)

            val screeningIds = screenings.map { it.id }.toList()
            val bookings = BookingTable.findBookings(screeningIds)
                .groupBy { ScreeningId(it.screeningId) }
                .mapValues { (_, bookings) -> bookings.map { it.toDomain() } }

            val roomIds = screenings.map { it.roomId }.toList()
            val rooms = roomService.findRooms(roomIds).associateBy { it.id }

            screenings.map {
                it.toDomain(
                    room = rooms[it.roomId] ?: error("Room ${it.roomId.value} not found (screening: ${it.id.value})"),
                    bookings = bookings[it.id].orEmpty(),
                )
            }.toList()
        }

    override suspend fun findScreeningByBooking(bookingId: BookingId): Screening? {
        val screeningData = findScreeningDataByBooking(bookingId)
            ?: return null

        val room = roomService.findRoom(screeningData.roomId)
            ?: error("Room of ID ${screeningData.roomId.value} not found (screening: ${screeningData.id.value})")

        val bookings = suspendTransaction { BookingTable.findBookings(screeningData.id).toList() }
        return screeningData.toDomain(room, bookings)
    }

    private suspend fun findScreeningDataByBooking(bookingId: BookingId): ScreeningData? =
        suspendTransaction {
            BookingTable.findScreeningIdByBooking(bookingId)
                ?.let { ScreeningTable.find(it) }
        }
}

private fun ScreeningData.toListItem(room: Room) = ScreeningListItemView(id, startTime, room.toListItemRoom())

private fun Room.toListItemRoom() = ScreeningListItemRoomView(id, number)
