package pl.kskarzynski.multiplex.api.validation

import arrow.core.EitherNel
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.springframework.stereotype.Service
import pl.kskarzynski.multiplex.common.util.extensions.accumulateErrors
import pl.kskarzynski.multiplex.common.util.extensions.isCapitalized
import pl.kskarzynski.multiplex.domain.model.user.UserName
import pl.kskarzynski.multiplex.domain.model.user.UserNameValidationError
import pl.kskarzynski.multiplex.domain.model.user.UserNameValidationError.InvalidNameCharacters
import pl.kskarzynski.multiplex.domain.model.user.UserNameValidationError.NameNotCapitalized
import pl.kskarzynski.multiplex.domain.model.user.UserNameValidationError.NameTooShort

@Service
class UserNameValidationImpl : UserNameValidation {

    override fun validateName(name: String): EitherNel<UserNameValidationError, UserName> =
        either {
            accumulateErrors(
                { ensure(name.length >= UserName.MIN_LENGTH) { NameTooShort } },
                { ensure(name.isCapitalized()) { NameNotCapitalized } },
                { ensure(name.all { it in UserName.VALID_CHARACTERS }) { InvalidNameCharacters } },
            )

            UserName(name)
        }
}
