package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

internal data class PersistentSeat(
    val row: Int,
    val number: Int,
)

internal fun PersistentSeat.toDomain() =
    Seat(
        row = SeatRow(row),
        number = SeatNumber(number),
    )

internal fun Seat.toPersistentSeat() =
    PersistentSeat(
        row = row.value,
        number = number.value,
    )
