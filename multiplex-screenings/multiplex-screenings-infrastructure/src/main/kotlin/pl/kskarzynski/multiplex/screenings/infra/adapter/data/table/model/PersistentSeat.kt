package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

data class PersistentSeat(
    val row: Int,
    val number: Int,
)

fun PersistentSeat.toDomain() =
    Seat(
        row = SeatRow(row),
        number = SeatNumber(number),
    )

fun Seat.toPersistentSeat() =
    PersistentSeat(
        row = row.value,
        number = number.value,
    )
