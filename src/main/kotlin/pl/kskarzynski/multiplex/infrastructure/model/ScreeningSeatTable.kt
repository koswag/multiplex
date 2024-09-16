package pl.kskarzynski.multiplex.infrastructure.model

import org.jetbrains.exposed.sql.Table

object ScreeningSeatTable : Table("SCREENING_SEATS") {
    val screeningId = reference("SCREENING_ID", ScreeningTable)
    val seatId = reference("SEAT_ID", SeatTable)
    val isTaken = bool("IS_TAKEN")
}
