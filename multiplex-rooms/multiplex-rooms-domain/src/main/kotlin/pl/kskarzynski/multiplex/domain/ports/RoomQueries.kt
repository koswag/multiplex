package pl.kskarzynski.multiplex.domain.ports

import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId

interface RoomQueries {
    suspend fun findRoom(roomId: RoomId): Room?
}