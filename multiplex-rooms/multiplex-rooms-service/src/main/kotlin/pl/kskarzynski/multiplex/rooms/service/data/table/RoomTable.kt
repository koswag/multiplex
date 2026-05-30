@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.rooms.service.data.table

import arrow.core.toNonEmptyListOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.upsert
import pl.kskarzynski.multiplex.common.infra.exposed.findById
import pl.kskarzynski.multiplex.common.infra.exposed.findOne
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import kotlin.uuid.ExperimentalUuidApi

object RoomTable : UuidTable("multiplex_rooms.rooms") {
    val number = integer("number")

    suspend fun findById(roomId: RoomId): Room? =
        findById(roomId.value)
            ?.let { rowToDomain(it) }

    fun findByIds(roomIds: Collection<RoomId>): Flow<Room> {
        val ids = roomIds.map { it.value }

        return selectAll()
            .where { id inList ids }
            .map { rowToDomain(it) }
    }

    suspend fun findByNumber(roomNumber: RoomNumber): Room? =
        findOne { number eq roomNumber.value }
            ?.let { rowToDomain(it) }

    suspend fun rowToDomain(row: ResultRow): Room {
        val roomId = RoomId(row[id].value)
        val seats = RoomSeatTable.findSeats(roomId).toList().toNonEmptyListOrNull()
            ?: error("Room of ID $roomId has no seats.")

        return Room(
            id = roomId,
            number = RoomNumber(row[number]),
            seats = seats,
        )
    }

    suspend fun save(room: Room) {
        upsert(id) {
            it[id] = room.id.value
            it[number] = room.number.value
        }

        RoomSeatTable.updateRoomSeats(room.id, room.seats)
    }
}
