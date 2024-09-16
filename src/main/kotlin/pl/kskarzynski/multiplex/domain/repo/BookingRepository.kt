package pl.kskarzynski.multiplex.domain.repo

import pl.kskarzynski.multiplex.domain.model.booking.BookingId
import pl.kskarzynski.multiplex.domain.model.booking.PricedBooking

interface BookingRepository {
    suspend fun saveBooking(booking: PricedBooking)
    suspend fun deleteBooking(bookingId: BookingId)
}
