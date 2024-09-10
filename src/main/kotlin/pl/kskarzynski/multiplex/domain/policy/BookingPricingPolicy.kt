package pl.kskarzynski.multiplex.domain.policy

import pl.kskarzynski.multiplex.domain.model.booking.Booking
import pl.kskarzynski.multiplex.domain.model.booking.BookingPrice

class BookingPricingPolicy {

    fun priceBooking(booking: Booking): BookingPrice =
        booking.tickets
            .sumOf { it.type.price.value }
            .let(::BookingPrice)
}
