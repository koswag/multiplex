package pl.kskarzynski.multiplex.domain.model.booking

import pl.kskarzynski.multiplex.domain.model.screening.SeatPlacement

sealed interface BookingValidationError {
    data object TooLateForBooking : BookingValidationError
    data object NoTickets : BookingValidationError
    data class NonExistentSeats(val seats: Collection<SeatPlacement>) : BookingValidationError
    data class SeatsAlreadyTaken(val seats: Collection<SeatPlacement>) : BookingValidationError
    data class SingleSeatsLeft(val seats: Collection<SeatPlacement>) : BookingValidationError
}

@JvmInline
value class ValidBooking(val inner: Booking)
