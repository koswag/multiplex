package pl.kskarzynski.multiplex.rooms.service.data

import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import pl.kskarzynski.multiplex.rooms.service.data.table.RoomTable
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber

class DatabaseRoomRepository : RoomRepository {

    override suspend fun save(room: Room) {
        suspendTransaction {
            RoomTable.save(room)
        }
    }

    override suspend fun findById(roomId: RoomId): Room? =
        suspendTransaction {
            RoomTable.findById(roomId)
        }

    override suspend fun findByNumber(number: RoomNumber): Room? =
        suspendTransaction {
            RoomTable.findByNumber(number)
        }

    override suspend fun findByIds(roomIds: Collection<RoomId>): List<Room> =
        suspendTransaction {
            RoomTable.findByIds(roomIds)
                .toList()
        }
}
