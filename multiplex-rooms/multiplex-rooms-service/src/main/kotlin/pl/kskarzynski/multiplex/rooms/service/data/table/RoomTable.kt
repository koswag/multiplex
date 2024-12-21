package pl.kskarzynski.multiplex.rooms.service.data.table

import arrow.core.toNonEmptyListOrNull
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.upsert
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber

object RoomTable : UUIDTable("multiplex_rooms.rooms") {
    val number = integer("number")

    context(Transaction)
    fun findRoom(roomId: RoomId): Room? =
        select(number)
            .where { id eq roomId.value }
            .firstOrNull()
            ?.let { row ->
                val seats = RoomSeatTable.findSeats(roomId).toNonEmptyListOrNull()
                    ?: error("Room of ID $roomId has no seats.")

                Room(
                    id = roomId,
                    number = RoomNumber(row[number]),
                    seats = seats,
                )
            }

    context(Transaction)
    fun saveRoom(room: Room) {
        upsert(id,
            onUpdate = listOf(
                number to intLiteral(room.number.value),
            )
        ) {
            it[id] = room.id.value
            it[number] = room.number.value
        }

        RoomSeatTable.updateRoomSeats(room.id, room.seats)
    }
}
