package pl.kskarzynski.multiplex.screenings.infra.rest

import arrow.core.NonEmptyList
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingConfirmationErrorDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingIdDto
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

sealed interface BookingConfirmationResult {
    data class ScreeningDoesNotExist(val screeningId: ScreeningId) : BookingConfirmationResult
    data class ConfirmationFailure(val errors: NonEmptyList<BookingConfirmationErrorDto>) : BookingConfirmationResult
    data class Success(val bookingId: BookingIdDto) : BookingConfirmationResult
}
