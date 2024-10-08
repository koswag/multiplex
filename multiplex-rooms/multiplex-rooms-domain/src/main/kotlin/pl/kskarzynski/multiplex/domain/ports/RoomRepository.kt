package pl.kskarzynski.multiplex.domain.ports

import pl.kskarzynski.multiplex.shared.room.Room

interface RoomRepository : RoomQueries {
    suspend fun saveRoom(room: Room)
}