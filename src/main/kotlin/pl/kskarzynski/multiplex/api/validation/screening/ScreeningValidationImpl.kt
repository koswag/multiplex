package pl.kskarzynski.multiplex.api.validation.screening

import arrow.core.EitherNel
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MINUTES
import org.springframework.stereotype.Service
import pl.kskarzynski.multiplex.api.validation.BookingValidationError
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.ScreeningValidationError.ScreeningDoesNotExist
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.ScreeningValidationError.TooLateForBooking
import pl.kskarzynski.multiplex.domain.model.booking.BookingRequest.Companion.MIN_TIME_BEFORE_SCREENING_IN_MINUTES
import pl.kskarzynski.multiplex.domain.model.screening.Screening
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningId
import pl.kskarzynski.multiplex.domain.queries.ScreeningQueries

// TODO: Tests
@Service
class ScreeningValidationImpl(
    private val screeningQueries: ScreeningQueries,
) : ScreeningValidation {

    override suspend fun validateBookedScreening(
        screeningId: ScreeningId,
        bookingTime: LocalDateTime
    ): EitherNel<BookingValidationError, Screening> =
        either {
            val screening = screeningQueries.findScreening(screeningId)
            ensureNotNull(screening) { ScreeningDoesNotExist(screeningId).nel() }

            ensure(MINUTES.between(bookingTime, screening.startTime) >= MIN_TIME_BEFORE_SCREENING_IN_MINUTES) {
                TooLateForBooking(bookingTime, screening.startTime).nel()
            }

            screening
        }
}
