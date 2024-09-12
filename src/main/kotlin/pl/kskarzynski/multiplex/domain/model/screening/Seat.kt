package pl.kskarzynski.multiplex.domain.model.screening

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import pl.kskarzynski.multiplex.domain.model.screening.BookingError.SeatAlreadyTaken

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
}
