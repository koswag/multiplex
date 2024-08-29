package pl.kskarzynski.multiplex.api.validation

import arrow.core.EitherNel
import pl.kskarzynski.multiplex.domain.model.user.UserSurname
import pl.kskarzynski.multiplex.domain.model.user.UserSurnameValidationError

fun interface UserSurnameValidation {
    fun validateSurname(surname: String): EitherNel<UserSurnameValidationError, UserSurname>
}
