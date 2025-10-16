package pl.kskarzynski.multiplex.screenings.domain.port.policy

import pl.kskarzynski.multiplex.common.utils.datetime.plusMinutes
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.shared.booking.BookingExpirationTime

class BookingExpirationPolicyImpl : BookingExpirationPolicy {

    override fun determineBookingExpirationTime(request: BookingRequest): BookingExpirationTime {
        val expirationTime = request.bookingTime.value plusMinutes BOOKING_EXPIRATION_PERIOD_IN_MINUTES
        return BookingExpirationTime(expirationTime)
    }

    companion object {
        const val BOOKING_EXPIRATION_PERIOD_IN_MINUTES = 15
    }
}
