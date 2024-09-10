package pl.kskarzynski.multiplex.api.validation.screening

import arrow.core.EitherNel
import java.time.LocalDateTime
import pl.kskarzynski.multiplex.api.validation.BookingValidationError
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningId
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningInfo

interface ScreeningValidation {
    suspend fun validateBookedScreening(
        screeningId: ScreeningId,
        bookingTime: LocalDateTime,
    ): EitherNel<BookingValidationError, ScreeningInfo>
}
