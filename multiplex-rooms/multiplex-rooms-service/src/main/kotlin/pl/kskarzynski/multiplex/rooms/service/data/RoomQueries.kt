package pl.kskarzynski.multiplex.rooms.service.data

import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber

interface RoomQueries {
    suspend fun findById(roomId: RoomId): Room?
    suspend fun findByNumber(number: RoomNumber): Room?
    suspend fun findByIds(roomIds: Collection<RoomId>): List<Room>
}
