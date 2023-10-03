package pl.kskarzynski.multiplex.domain.services.pricing

import pl.kskarzynski.multiplex.domain.model.booking.BookingPrice
import pl.kskarzynski.multiplex.domain.model.booking.ValidBooking

interface BookingPricingPolicy {

    operator fun invoke(booking: ValidBooking): BookingPrice =
        booking.inner.tickets
            .sumOf { it.type.price.value }
            .let(::BookingPrice)

}
