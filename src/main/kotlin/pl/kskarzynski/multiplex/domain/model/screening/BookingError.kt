package pl.kskarzynski.multiplex.domain.model.screening

import pl.kskarzynski.multiplex.shared.screening.SeatPlacement

interface BookingError {
    data class SeatDoesNotExist(val seatPlacement: SeatPlacement) : BookingError
    data class SeatAlreadyTaken(val seatPlacement: SeatPlacement) : BookingError
    data class SeatIsSingle(val seatPlacement: SeatPlacement) : BookingError
}
