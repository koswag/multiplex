package pl.kskarzynski.multiplex.rooms.service.util

import pl.kskarzynski.multiplex.rooms.service.data.RoomRepository
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber

class FakeRoomRepository : RoomRepository {

    private val rooms = mutableMapOf<RoomId, Room>()

    override suspend fun save(room: Room) {
        rooms[room.id] = room
    }

    override suspend fun findById(roomId: RoomId): Room? = rooms[roomId]

    override suspend fun findByNumber(number: RoomNumber): Room? =
        rooms.values.find { it.number == number }

    override suspend fun findByIds(roomIds: Collection<RoomId>): List<Room> {
        val ids = roomIds.toSet()
        return rooms.values.filter { it.id in ids }
    }
}
