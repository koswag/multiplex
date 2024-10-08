package pl.kskarzynski.multiplex.domain.model.screening

import pl.kskarzynski.multiplex.shared.room.Seat

interface BookingError {
    data class SeatDoesNotExist(val seatPlacement: Seat) : BookingError
    data class SeatAlreadyTaken(val seatPlacement: Seat) : BookingError
    data class SeatIsSingle(val seatPlacement: Seat) : BookingError
}
