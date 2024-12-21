package pl.kskarzynski.multiplex.rooms.service.data

import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId

interface RoomQueries {
    suspend fun findRoom(roomId: RoomId): Room?
}