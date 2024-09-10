package pl.kskarzynski.multiplex.api.validation.user

import arrow.core.EitherNel
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserSurnameValidationError
import pl.kskarzynski.multiplex.domain.model.user.UserSurname

fun interface UserSurnameValidation {
    fun validateSurname(surname: String): EitherNel<UserSurnameValidationError, UserSurname>
}
