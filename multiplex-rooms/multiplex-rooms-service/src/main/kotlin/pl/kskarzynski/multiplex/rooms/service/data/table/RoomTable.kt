package pl.kskarzynski.multiplex.rooms.service.data.table

import arrow.core.toNonEmptyListOrNull
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.upsert
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber

object RoomTable : UUIDTable("multiplex_rooms.rooms") {
    val number = integer("number")

    context(Transaction)
    fun findById(roomId: RoomId): Room? =
        findOne { id eq roomId.value }

    context(Transaction)
    fun findByNumber(roomNumber: RoomNumber): Room? =
        findOne { number eq roomNumber.value }

    context(Transaction)
    private fun findOne(predicate: SqlExpressionBuilder.() -> Op<Boolean>): Room? =
        select(id, number)
            .where(predicate)
            .firstOrNull()
            ?.let { rowToDomain(it) }

    context(Transaction)
    private fun rowToDomain(row: ResultRow): Room {
        val roomId = RoomId(row[id].value)
        val seats = RoomSeatTable.findSeats(roomId).toNonEmptyListOrNull()
            ?: error("Room of ID $roomId has no seats.")

        return Room(
            id = roomId,
            number = RoomNumber(row[number]),
            seats = seats,
        )
    }

    context(Transaction)
    fun save(room: Room) {
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
