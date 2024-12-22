package pl.kskarzynski.multiplex.rooms.service.data

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pl.kskarzynski.multiplex.rooms.service.data.table.RoomTable
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber

class DatabaseRoomRepository : RoomRepository {

    override suspend fun save(room: Room) {
        newSuspendedTransaction {
            RoomTable.save(room)
        }
    }

    override suspend fun findById(roomId: RoomId): Room? =
        newSuspendedTransaction {
            RoomTable.findById(roomId)
        }

    override suspend fun findByNumber(number: RoomNumber): Room? =
        newSuspendedTransaction {
            RoomTable.findByNumber(number)
        }
}
