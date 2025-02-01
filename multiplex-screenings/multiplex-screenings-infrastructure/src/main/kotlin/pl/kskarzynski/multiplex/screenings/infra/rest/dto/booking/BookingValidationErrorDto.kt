package pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking

import kotlinx.serialization.Serializable
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserName
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserSurname
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

@Serializable
sealed interface BookingValidationErrorDto {

    @Serializable
    data class UserNameTooShort(val userName: String) : BookingValidationErrorDto {
        val minLength = UserName.Companion.MIN_LENGTH
    }

    @Serializable
    data class UserNameNotCapitalized(val userName: String) : BookingValidationErrorDto

    @Serializable
    data class UserNameContainsIllegalCharacters(
        val userName: String,
        val illegalCharacters: List<Char>,
    ) : BookingValidationErrorDto

    @Serializable
    data class UserSurnameTooShort(val userSurname: String) : BookingValidationErrorDto {
        val minLength = UserSurname.Companion.MIN_LENGTH
    }

    @Serializable
    data class UserSurnameNotCapitalized(val userSurname: String) : BookingValidationErrorDto

    @Serializable
    data class UserSurnameHasTooManyHyphens(val userSurname: String) : BookingValidationErrorDto {
        val maxHyphenCount = UserSurname.Companion.MAX_HYPHEN_COUNT
    }

    @Serializable
    data class UserSurnameSecondPartIsNotCapitalized(val userSurname: String) : BookingValidationErrorDto

    @Serializable
    data class UserSurnameContainsIllegalCharacters(
        val userSurname: String,
        val illegalCharacters: List<Char>,
    ) : BookingValidationErrorDto

    @Serializable
    data class IllegalSeatRow(val row: Int) : BookingValidationErrorDto {
        val minValue = SeatRow.Companion.MIN_VALUE
    }

    @Serializable
    data class IllegalSeatNumber(val number: Int) : BookingValidationErrorDto {
        val minValue = SeatNumber.Companion.MIN_VALUE
    }
}
