package pl.kskarzynski.multiplex.screenings.infra.rest

import arrow.core.NonEmptyList
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingErrorDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingIdDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

sealed interface BookingResult {
    data class ScreeningDoesNotExist(val screeningId: ScreeningId) : BookingResult
    data class ValidationFailure(val validationErrors: NonEmptyList<BookingValidationErrorDto>) : BookingResult
    data class BookingFailure(val bookingErrors: NonEmptyList<BookingErrorDto>) : BookingResult
    data class Success(val bookingId: BookingIdDto) : BookingResult
}
