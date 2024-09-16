package pl.kskarzynski.multiplex.infrastructure.model

import org.jetbrains.exposed.dao.id.UUIDTable

object SeatTable : UUIDTable("SEATS") {
    val roomNumber = reference("ROOM_NO", RoomTable)
    val seatRow = integer("ROW")
    val seatNumber = integer("NUMBER")
}
