package pl.kskarzynski.multiplex.domain.queries

import pl.kskarzynski.multiplex.domain.model.booking.Booking
import pl.kskarzynski.multiplex.domain.model.booking.BookingId

interface BookingQueries {
    suspend fun findBooking(bookingId: BookingId): Booking?
}
