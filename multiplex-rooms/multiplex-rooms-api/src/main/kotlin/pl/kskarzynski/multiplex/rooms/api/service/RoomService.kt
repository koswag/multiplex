package pl.kskarzynski.multiplex.rooms.api.service

import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId

interface RoomService {
    suspend fun findRoom(roomId: RoomId): Room?
}
