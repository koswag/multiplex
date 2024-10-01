package pl.kskarzynski.multiplex.infrastructure.repo

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Repository
import pl.kskarzynski.multiplex.domain.model.booking.Booking
import pl.kskarzynski.multiplex.domain.model.booking.BookingId
import pl.kskarzynski.multiplex.domain.repo.BookingRepository
import pl.kskarzynski.multiplex.infrastructure.model.BookingTable

@Repository
class DatabaseBookingRepository : BookingRepository {

    override suspend fun saveBooking(booking: Booking) {
        newSuspendedTransaction {
            BookingTable.insert(booking)
        }
    }

    override suspend fun deleteBooking(bookingId: BookingId) {
        newSuspendedTransaction {
            BookingTable.delete(bookingId)
        }
    }

    override suspend fun findBooking(bookingId: BookingId): Booking? =
        newSuspendedTransaction {
            BookingTable.findBooking(bookingId)
        }
}
