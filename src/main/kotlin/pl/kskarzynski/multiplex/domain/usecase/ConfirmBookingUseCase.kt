package pl.kskarzynski.multiplex.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import java.time.Clock
import pl.kskarzynski.multiplex.common.utils.datetime.currentTime
import pl.kskarzynski.multiplex.domain.model.booking.Booking.CancelledBooking
import pl.kskarzynski.multiplex.domain.model.booking.Booking.ConfirmedBooking
import pl.kskarzynski.multiplex.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.domain.model.booking.BookingConfirmationError
import pl.kskarzynski.multiplex.domain.model.booking.BookingConfirmationError.BookingDoesNotExist
import pl.kskarzynski.multiplex.domain.model.booking.BookingConfirmationError.BookingExpired
import pl.kskarzynski.multiplex.domain.repo.BookingRepository
import pl.kskarzynski.multiplex.shared.booking.BookingId

class ConfirmBookingUseCase(
    private val bookingRepository: BookingRepository,
    private val clock: Clock,
) {
    suspend fun execute(bookingId: BookingId): Either<BookingConfirmationError, ConfirmedBooking> =
        either {
            val booking = bookingRepository.findBooking(bookingId)
            ensureNotNull(booking) { BookingDoesNotExist(bookingId) }

            when (booking) {
                is CancelledBooking -> raise(BookingExpired(booking.expirationTime))
                is ConfirmedBooking -> booking
                is UnconfirmedBooking -> {
                    val confirmedBooking = booking.confirm(currTime = clock.currentTime()).bind()
                    confirmedBooking.also { bookingRepository.saveBooking(it) }
                }
            }
        }
}
