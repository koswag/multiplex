package pl.kskarzynski.multiplex.domain.usecase

import pl.kskarzynski.multiplex.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.domain.repo.BookingRepository

class CancelBookingUseCase(
    private val bookingRepository: BookingRepository,
) {
    suspend fun execute(booking: UnconfirmedBooking) {
        val cancelled = booking.cancel()
        bookingRepository.saveBooking(cancelled)
    }
}
