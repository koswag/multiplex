package pl.kskarzynski.multiplex.screenings.domain.port.data

import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

interface ScreeningQueries {
    suspend fun findScreening(screeningId: ScreeningId): Screening?
    suspend fun findScreeningsWithExpiredBookings(): List<Screening>
    suspend fun findScreeningByBooking(bookingId: BookingId): Screening?
}
