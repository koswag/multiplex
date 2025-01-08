package pl.kskarzynski.multiplex.rooms.service.util

import pl.kskarzynski.multiplex.rooms.service.data.RoomRepository
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber

class FakeRoomRepository : RoomRepository {

    private val rooms = mutableMapOf<RoomId, Room>()

    override suspend fun save(room: Room) {
        rooms.put(room.id, room)
    }

    override suspend fun findById(roomId: RoomId): Room? = rooms[roomId]

    override suspend fun findByNumber(number: RoomNumber): Room? =
        rooms.values.find { it.number == number }
}
