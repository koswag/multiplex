package pl.kskarzynski.multiplex.infrastructure.model

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningRoom
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber

object ScreeningRoomTable : UUIDTable("ROOMS") {
    val number = integer("number")

    fun rowToDomain(row: ResultRow) =
        ScreeningRoom(
            id = RoomId(row[id].value),
            number = RoomNumber(row[number]),
            seats = emptyList(), // TODO
        )
}
