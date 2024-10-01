package pl.kskarzynski.multiplex.domain.policy

import pl.kskarzynski.multiplex.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.shared.booking.BookingPrice

class BookingPricingPolicy {

    fun priceBooking(bookingRequest: BookingRequest): BookingPrice {
        val ticketPriceSum = bookingRequest.tickets.sumOf { it.type.price.value }
        return BookingPrice(ticketPriceSum)
    }
}
