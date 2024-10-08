package pl.kskarzynski.multiplex.rooms.infra.adapter.data.table

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

object RoomSeatTable : Table("multiplex_rooms.seats") {
    val roomId = reference("room_id", RoomTable)
    val row = integer("row")
    val number = integer("number")

    override val primaryKey = PrimaryKey(roomId, row, number)

    context(Transaction)
    fun findSeats(id: RoomId): List<Seat> =
        select(row, number)
            .where { roomId eq id.value }
            .map { rowToDomain(it) }

    context(Transaction)
    private fun rowToDomain(resultRow: ResultRow) =
        Seat(
            row = SeatRow(resultRow[row]),
            number = SeatNumber(resultRow[number]),
        )

    context(Transaction)
    fun updateRoomSeats(id: RoomId, seats: List<Seat>) {
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
