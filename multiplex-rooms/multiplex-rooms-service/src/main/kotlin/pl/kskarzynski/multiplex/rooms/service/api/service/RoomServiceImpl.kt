package pl.kskarzynski.multiplex.rooms.service.api.service

import pl.kskarzynski.multiplex.rooms.api.service.RoomService
import pl.kskarzynski.multiplex.rooms.service.data.RoomQueries
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId

class RoomServiceImpl(
    private val roomQueries: RoomQueries,
) : RoomService {

    override suspend fun findRoom(roomId: RoomId): Room? =
        roomQueries.findById(roomId)
}
