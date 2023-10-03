package pl.kskarzynski.multiplex.domain.repo

import pl.kskarzynski.multiplex.domain.model.booking.BookingId
import pl.kskarzynski.multiplex.domain.model.booking.PricedBooking

interface BookingRepository {
    fun saveBooking(booking: PricedBooking)
    fun deleteBooking(bookingId: BookingId)
}
