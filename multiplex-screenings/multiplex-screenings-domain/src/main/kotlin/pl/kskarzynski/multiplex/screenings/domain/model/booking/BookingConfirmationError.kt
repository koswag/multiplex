package pl.kskarzynski.multiplex.screenings.domain.model.booking

import pl.kskarzynski.multiplex.shared.booking.BookingExpirationTime
import pl.kskarzynski.multiplex.shared.booking.BookingId

sealed interface BookingConfirmationError {
    data class BookingDoesNotExist(val bookingId: BookingId) : BookingConfirmationError
    data class BookingExpired(val expiredAt: BookingExpirationTime) : BookingConfirmationError
}
