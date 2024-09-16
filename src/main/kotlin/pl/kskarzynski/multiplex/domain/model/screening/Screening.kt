package pl.kskarzynski.multiplex.domain.model.screening

import arrow.core.EitherNel
import arrow.core.raise.either
import java.time.LocalDateTime

data class Screening(
    val id: ScreeningId,
    val startTime: LocalDateTime,
    val room: ScreeningRoom,
) {
    fun bookSeats(seats: List<SeatPlacement>): EitherNel<BookingError, Screening> =
        either {
            val updatedRoom = room.bookSeats(seats).bind()
            copy(room = updatedRoom)
        }
}
