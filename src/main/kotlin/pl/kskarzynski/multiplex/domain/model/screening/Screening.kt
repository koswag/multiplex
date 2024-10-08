package pl.kskarzynski.multiplex.domain.model.screening

import arrow.core.EitherNel
import arrow.core.raise.either
import java.time.LocalDateTime
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

data class Screening(
    val id: ScreeningId,
    val movieId: MovieId,
    val startTime: LocalDateTime,
    val room: ScreeningRoom,
) {
    fun bookSeats(seats: List<Seat>): EitherNel<BookingError, Screening> =
        either {
            val updatedRoom = room.bookSeats(seats).bind()
            copy(room = updatedRoom)
        }

    fun cancelBooking(seats: List<Seat>): Screening {
        val updatedRoom = room.cancelBooking(seats)
        return copy(room = updatedRoom)
    }
}
