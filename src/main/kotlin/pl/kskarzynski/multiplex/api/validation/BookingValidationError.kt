package pl.kskarzynski.multiplex.api.validation

import java.time.LocalDateTime
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningId

sealed interface BookingValidationError {

    sealed interface UserNameValidationError : BookingValidationError {
        data object NameTooShort : UserNameValidationError
        data object NameNotCapitalized : UserNameValidationError
        data class InvalidNameCharacters(val invalidCharacters: List<Char>) : UserNameValidationError
    }

    sealed interface UserSurnameValidationError : BookingValidationError {
        data object SurnameTooShort : UserSurnameValidationError
        data object SurnameNotCapitalized : UserSurnameValidationError
        data object SurnameHasTooManyHyphens : UserSurnameValidationError
        data object SurnameSecondPartIsNotCapitalized : UserSurnameValidationError
        data class InvalidSurnameCharacters(val invalidCharacters: List<Char>) : UserSurnameValidationError
    }

    sealed interface ScreeningValidationError : BookingValidationError {
        data class ScreeningDoesNotExist(val screeningId: ScreeningId) : ScreeningValidationError
        data class TooLateForBooking(
            val bookingTime: LocalDateTime,
            val screeningStartTime: LocalDateTime,
        ) : ScreeningValidationError
    }

    sealed interface TicketValidationError : BookingValidationError {
        data object NoTickets : TicketValidationError
        data class InvalidRowNumber(val rowNumber: Int) : TicketValidationError
        data class InvalidSeatNumber(val seatNumber: Int) : TicketValidationError
    }
}
