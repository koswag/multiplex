package pl.kskarzynski.multiplex.rooms.service.data

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pl.kskarzynski.multiplex.rooms.service.data.table.RoomTable
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId

class DatabaseRoomRepository : RoomRepository {

    override suspend fun saveRoom(room: Room) {
        newSuspendedTransaction {
            RoomTable.saveRoom(room)
        }
    }

    override suspend fun findRoom(roomId: RoomId): Room? =
        newSuspendedTransaction {
            RoomTable.findRoom(roomId)
        }
}
