package pl.kskarzynski.multiplex.domain.policy

import pl.kskarzynski.multiplex.domain.model.booking.Booking
import pl.kskarzynski.multiplex.domain.model.booking.BookingExpirationTime

class BookingExpirationPolicy {

    fun determineBookingExpirationTime(booking: Booking): BookingExpirationTime {
        val expirationTime = booking.bookedAt.plusMinutes(BOOKING_LIFESPAN_IN_MINUTES.toLong())
        return BookingExpirationTime(expirationTime)
    }

    companion object {
        const val BOOKING_LIFESPAN_IN_MINUTES = 15
    }
}
