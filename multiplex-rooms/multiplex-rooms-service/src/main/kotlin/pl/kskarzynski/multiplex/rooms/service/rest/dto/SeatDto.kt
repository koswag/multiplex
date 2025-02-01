package pl.kskarzynski.multiplex.rooms.service.rest.dto

import kotlinx.serialization.Serializable
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

@Serializable
data class SeatDto(
    val row: Int,
    val number: Int,
)

fun Seat.toDto() =
    SeatDto(
        row = row.value,
        number = number.value,
    )

fun SeatDto.toDomain() =
    Seat(
        row = SeatRow(row),
        number = SeatNumber(number),
    )
