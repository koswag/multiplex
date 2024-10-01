package pl.kskarzynski.multiplex.domain.model.booking

sealed interface BookingConfirmationError {
    data class BookingDoesNotExist(val bookingId: BookingId) : BookingConfirmationError
    data class BookingExpired(val expiredAt: BookingExpirationTime) : BookingConfirmationError
}
