package pl.kskarzynski.multiplex.domain.model.booking

import pl.kskarzynski.multiplex.shared.booking.BookingId

sealed interface BookingConfirmationError {
    data class BookingDoesNotExist(val bookingId: BookingId) : BookingConfirmationError
    data class BookingExpired(val expiredAt: BookingExpirationTime) : BookingConfirmationError
}
