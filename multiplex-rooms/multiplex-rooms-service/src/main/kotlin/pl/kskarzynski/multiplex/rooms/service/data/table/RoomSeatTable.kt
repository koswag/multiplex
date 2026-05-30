@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.rooms.service.data.table

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow
import kotlin.uuid.ExperimentalUuidApi

object RoomSeatTable : Table("multiplex_rooms.seats") {
    val roomId = reference("room_id", RoomTable)
    val row = integer("row")
    val number = integer("number")

    override val primaryKey = PrimaryKey(roomId, row, number)

    fun findSeats(id: RoomId): Flow<Seat> =
        select(row, number)
            .where { roomId eq id.value }
            .map { rowToDomain(it) }

    private fun rowToDomain(resultRow: ResultRow) =
        Seat(
            row = SeatRow(resultRow[row]),
            number = SeatNumber(resultRow[number]),
        )

    suspend fun updateRoomSeats(id: RoomId, seats: List<Seat>) {
        deleteWhere { roomId eq id.value }

        for (seat in seats) {
            insert {
                it[roomId] = id.value
                it[row] = seat.row.value
                it[number] = seat.number.value
            }
        }
    }
}
