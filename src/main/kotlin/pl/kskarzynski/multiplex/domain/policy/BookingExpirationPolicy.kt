package pl.kskarzynski.multiplex.domain.policy

import pl.kskarzynski.multiplex.domain.model.booking.BookingExpirationTime
import pl.kskarzynski.multiplex.domain.model.booking.BookingRequest

class BookingExpirationPolicy {

    fun determineBookingExpirationTime(bookingRequest: BookingRequest): BookingExpirationTime {
        val expirationTime = bookingRequest.bookingTime.value.plusMinutes(BOOKING_LIFESPAN_IN_MINUTES.toLong())
        return BookingExpirationTime(expirationTime)
    }

    companion object {
        const val BOOKING_LIFESPAN_IN_MINUTES = 15
    }
}
