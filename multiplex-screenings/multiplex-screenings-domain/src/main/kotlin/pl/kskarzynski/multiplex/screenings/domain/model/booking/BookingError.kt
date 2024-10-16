package pl.kskarzynski.multiplex.screenings.domain.model.booking

import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

interface BookingError {
    data class ScreeningDoesNotExist(val screeningId: ScreeningId) : BookingError
    data class SeatDoesNotExist(val seatPlacement: Seat) : BookingError
    data class SeatAlreadyTaken(val seatPlacement: Seat) : BookingError
    data class SeatIsSingle(val seatPlacement: Seat) : BookingError
}
