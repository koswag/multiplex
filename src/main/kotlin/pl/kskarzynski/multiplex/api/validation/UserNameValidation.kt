package pl.kskarzynski.multiplex.api.validation

import arrow.core.EitherNel
import pl.kskarzynski.multiplex.domain.model.user.UserName
import pl.kskarzynski.multiplex.domain.model.user.UserNameValidationError

fun interface UserNameValidation {
    fun validateName(name: String): EitherNel<UserNameValidationError, UserName>
}
