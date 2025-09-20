package pl.kskarzynski.multiplex.screenings.domain.port.policy

import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.shared.booking.BookingPrice

interface BookingPricingPolicy {
    fun priceBooking(bookingRequest: BookingRequest, screening: Screening): BookingPrice
}
