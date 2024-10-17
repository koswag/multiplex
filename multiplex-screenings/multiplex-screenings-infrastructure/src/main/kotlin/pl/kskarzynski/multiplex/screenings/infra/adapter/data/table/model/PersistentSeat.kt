package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

data class PersistentSeat(
    val row: Int,
    val number: Int,
) {
    fun toDomain() =
        Seat(
            row = SeatRow(row),
            number = SeatNumber(number),
        )

    companion object {
        fun from(seat: Seat) =
            PersistentSeat(
                row = seat.row.value,
                number = seat.number.value,
            )
    }
}
