package pl.kskarzynski.multiplex.rooms.infra.adapter.api.service

import pl.kskarzynski.multiplex.domain.ports.RoomQueries
import pl.kskarzynski.multiplex.rooms.api.service.RoomService
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId

class RoomServiceImpl(
    private val roomQueries: RoomQueries,
) : RoomService {

    override suspend fun findRoom(roomId: RoomId): Room? =
        roomQueries.findRoom(roomId)
}
