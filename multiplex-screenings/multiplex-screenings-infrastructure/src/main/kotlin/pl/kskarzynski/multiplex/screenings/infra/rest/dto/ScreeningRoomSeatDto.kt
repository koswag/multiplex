package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import kotlinx.serialization.Serializable
import pl.kskarzynski.multiplex.shared.room.Seat

@Serializable
data class ScreeningRoomSeatDto(
    val row: Int,
    val number: Int,
    val isTaken: Boolean,
)

fun Seat.toDto(takenSeats: Collection<Seat>): ScreeningRoomSeatDto =
    ScreeningRoomSeatDto(
        row = row.value,
        number = number.value,
        isTaken = this in takenSeats,
    )
