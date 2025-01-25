package pl.kskarzynski.multiplex.rooms.service.data.table

import arrow.core.toNonEmptyListOrNull
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import pl.kskarzynski.multiplex.common.infra.exposed.findById
import pl.kskarzynski.multiplex.common.infra.exposed.findOne
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber

object RoomTable : UUIDTable("multiplex_rooms.rooms") {
    val number = integer("number")

    fun findById(roomId: RoomId): Room? =
        findById(roomId.value)
            ?.let { rowToDomain(it) }

    fun findByIds(roomIds: Collection<RoomId>): List<Room> {
        val ids = roomIds.map { it.value }

        return selectAll()
            .where { id inList ids }
            .map { rowToDomain(it) }
    }

    fun findByNumber(roomNumber: RoomNumber): Room? =
        findOne { number eq roomNumber.value }
            ?.let { rowToDomain(it) }

    fun rowToDomain(row: ResultRow): Room {
        val roomId = RoomId(row[id].value)
        val seats = RoomSeatTable.findSeats(roomId).toNonEmptyListOrNull()
            ?: error("Room of ID $roomId has no seats.")

        return Room(
            id = roomId,
            number = RoomNumber(row[number]),
            seats = seats,
        )
    }

    fun save(room: Room) {
        upsert(id) {
            it[id] = room.id.value
            it[number] = room.number.value
        }

        RoomSeatTable.updateRoomSeats(room.id, room.seats)
    }
}
