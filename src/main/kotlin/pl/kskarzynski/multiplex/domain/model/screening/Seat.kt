package pl.kskarzynski.multiplex.domain.model.screening

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import pl.kskarzynski.multiplex.domain.model.screening.BookingError.SeatAlreadyTaken
import pl.kskarzynski.multiplex.shared.screening.SeatPlacement

data class Seat(
    val placement: SeatPlacement,
    val isTaken: Boolean,
) {
    val isAvailable: Boolean = !isTaken

    fun markAsTaken(): Either<SeatAlreadyTaken, Seat> =
        either {
            ensure(!isTaken) { SeatAlreadyTaken(placement) }
            copy(isTaken = true)
        }

    fun markAsNotTaken(): Seat {
        check(isTaken) { "Seat (${placement.row}, ${placement.number}) is not taken" }
        return copy(isTaken = false)
    }
}
