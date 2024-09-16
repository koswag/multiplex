package pl.kskarzynski.multiplex.domain.model.booking

data class PricedBooking(
    val booking: Booking,
    val totalPrice: BookingPrice,
    val expiresAt: BookingExpirationTime,
)
