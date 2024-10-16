package pl.kskarzynski.multiplex.screenings.domain.port.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import java.time.Clock
import pl.kskarzynski.multiplex.common.utils.datetime.currentTime
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingConfirmationError
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingConfirmationError.BookingDoesNotExist
import pl.kskarzynski.multiplex.screenings.domain.port.data.ScreeningRepository
import pl.kskarzynski.multiplex.shared.booking.BookingId

class ConfirmBookingUseCase(
    private val screeningRepository: ScreeningRepository,
    private val clock: Clock,
) {
    suspend fun execute(bookingId: BookingId): Either<BookingConfirmationError, Unit> =
        either {
            val screening = screeningRepository.findScreeningByBooking(bookingId)
            ensureNotNull(screening) { BookingDoesNotExist(bookingId) }

            val updatedScreening = screening.confirmBooking(bookingId, clock.currentTime()).bind()

            screeningRepository.saveScreening(updatedScreening)
        }
}
