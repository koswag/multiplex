package pl.kskarzynski.multiplex.rooms.service.data

import pl.kskarzynski.multiplex.shared.room.Room

interface RoomRepository : RoomQueries {
    suspend fun save(room: Room)
}