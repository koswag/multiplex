package pl.kskarzynski.multiplex.api.validation.user

import arrow.core.EitherNel
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.springframework.stereotype.Service
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserNameValidationError
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserNameValidationError.InvalidNameCharacters
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserNameValidationError.NameNotCapitalized
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserNameValidationError.NameTooShort
import pl.kskarzynski.multiplex.domain.model.user.UserName
import pl.kskarzynski.multiplex.utils.arrow.accumulateErrors
import pl.kskarzynski.multiplex.utils.strings.isCapitalized

@Service
class UserNameValidationImpl : UserNameValidation {

    override fun validateName(name: String): EitherNel<UserNameValidationError, UserName> =
        either {
            val invalidCharacters = name.filter { it !in UserName.VALID_CHARACTERS }

            accumulateErrors(
                { ensure(name.length >= UserName.MIN_LENGTH) { NameTooShort } },
                { ensure(name.isCapitalized()) { NameNotCapitalized } },
                { ensure(invalidCharacters.isEmpty()) { InvalidNameCharacters(invalidCharacters.toList()) } },
            )

            UserName(name)
        }
}
