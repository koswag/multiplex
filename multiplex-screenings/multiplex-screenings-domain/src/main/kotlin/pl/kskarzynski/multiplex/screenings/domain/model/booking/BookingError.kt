package pl.kskarzynski.multiplex.screenings.domain.model.booking

import pl.kskarzynski.multiplex.shared.room.Seat

sealed interface BookingError {
    data class SeatDoesNotExist(val seat: Seat) : BookingError
    data class SeatAlreadyTaken(val seat: Seat) : BookingError
    data class SingleSeatLeft(val singleSeat: Seat) : BookingError
}
