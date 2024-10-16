package pl.kskarzynski.multiplex.screenings.domain.port.policy

import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.shared.booking.BookingPrice

class BookingPricingPolicyImpl : BookingPricingPolicy {

    override fun priceBooking(bookingRequest: BookingRequest): BookingPrice {
        val totalPrice = bookingRequest.tickets.sumOf { it.basePrice.value }
        return BookingPrice(totalPrice)
    }
}
