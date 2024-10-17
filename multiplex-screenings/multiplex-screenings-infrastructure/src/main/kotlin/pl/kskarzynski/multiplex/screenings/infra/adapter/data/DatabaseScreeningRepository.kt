package pl.kskarzynski.multiplex.screenings.infra.adapter.data

import java.time.Clock
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pl.kskarzynski.multiplex.common.utils.datetime.currentTime
import pl.kskarzynski.multiplex.rooms.api.service.RoomService
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.port.data.ScreeningRepository
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.ScreeningTable
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

class DatabaseScreeningRepository(
    private val roomService: RoomService,
    private val clock: Clock,
) : ScreeningRepository {

    override suspend fun saveScreening(screening: Screening) {
        newSuspendedTransaction {
            ScreeningTable.saveScreening(screening)
        }
    }

    override suspend fun findScreening(screeningId: ScreeningId): Screening? {
        val screeningData =
            newSuspendedTransaction { ScreeningTable.findScreening(screeningId) }
                ?: return null

        val room =
            roomService.findRoom(screeningData.roomId)
                ?: error("Room of ID ${screeningData.roomId.value} not found (screening: ${screeningId.value})")

        return screeningData.toDomain(room)
    }

    override suspend fun findScreeningsWithExpiredBookings(): List<Screening> =
        newSuspendedTransaction {
            ScreeningTable.findScreeningsWithExpiredBookings(clock.currentTime())
                .map { screeningData ->
                    // TODO: Optimize
                    val room =
                        roomService.findRoom(screeningData.roomId)
                            ?: error("Room of ID ${screeningData.roomId.value} not found (screening: ${screeningData.id.value})")

                    screeningData.toDomain(room)
                }
        }

    override suspend fun findScreeningByBooking(bookingId: BookingId): Screening? =
        newSuspendedTransaction {
            val screeningData = ScreeningTable.findScreeningByBooking(bookingId)
                ?: return@newSuspendedTransaction null

            val room =
                roomService.findRoom(screeningData.roomId)
                    ?: error("Room of ID ${screeningData.roomId.value} not found (screening: ${screeningData.id.value})")

            screeningData.toDomain(room)
        }
}
