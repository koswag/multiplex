package pl.kskarzynski.multiplex.api.validation.user

import arrow.core.EitherNel
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserNameValidationError
import pl.kskarzynski.multiplex.domain.model.user.UserName

fun interface UserNameValidation {
    fun validateName(name: String): EitherNel<UserNameValidationError, UserName>
}
