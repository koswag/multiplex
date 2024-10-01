package pl.kskarzynski.multiplex.domain.policy

import pl.kskarzynski.multiplex.domain.model.booking.BookingPrice
import pl.kskarzynski.multiplex.domain.model.booking.BookingRequest

class BookingPricingPolicy {

    fun priceBooking(bookingRequest: BookingRequest): BookingPrice =
        bookingRequest.tickets
            .sumOf { it.type.price.value }
            .let(::BookingPrice)
}
