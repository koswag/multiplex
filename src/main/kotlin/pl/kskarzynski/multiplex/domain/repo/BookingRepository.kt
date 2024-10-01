package pl.kskarzynski.multiplex.domain.repo

import pl.kskarzynski.multiplex.domain.model.booking.Booking
import pl.kskarzynski.multiplex.domain.queries.BookingQueries
import pl.kskarzynski.multiplex.shared.booking.BookingId

interface BookingRepository : BookingQueries {
    suspend fun saveBooking(booking: Booking)
    suspend fun deleteBooking(bookingId: BookingId)
}
