package pl.kskarzynski.multiplex.infrastructure.repo

import org.springframework.stereotype.Repository
import pl.kskarzynski.multiplex.domain.model.booking.BookingId
import pl.kskarzynski.multiplex.domain.model.booking.PricedBooking
import pl.kskarzynski.multiplex.domain.repo.BookingRepository

@Repository
class DatabaseBookingRepository : BookingRepository {

    override suspend fun saveBooking(booking: PricedBooking) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBooking(bookingId: BookingId) {
        TODO("Not yet implemented")
    }
}
