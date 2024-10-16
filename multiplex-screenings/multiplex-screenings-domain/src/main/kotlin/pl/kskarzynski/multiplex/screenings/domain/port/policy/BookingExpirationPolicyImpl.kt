package pl.kskarzynski.multiplex.screenings.domain.port.policy

import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.shared.booking.BookingExpirationTime

class BookingExpirationPolicyImpl : BookingExpirationPolicy {

    override fun determineBookingExpirationTime(bookingRequest: BookingRequest): BookingExpirationTime {
        val expirationTime =
            bookingRequest.bookingTime.value
                .plusMinutes(BOOKING_LIFESPAN_IN_MINUTES.toLong())

        return BookingExpirationTime(expirationTime)
    }

    companion object {
        const val BOOKING_LIFESPAN_IN_MINUTES = 15
    }
}