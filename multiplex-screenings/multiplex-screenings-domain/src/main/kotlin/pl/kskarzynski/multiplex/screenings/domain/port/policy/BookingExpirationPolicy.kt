package pl.kskarzynski.multiplex.screenings.domain.port.policy

import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.shared.booking.BookingExpirationTime

interface BookingExpirationPolicy {
    fun determineBookingExpirationTime(bookingRequest: BookingRequest): BookingExpirationTime
}
