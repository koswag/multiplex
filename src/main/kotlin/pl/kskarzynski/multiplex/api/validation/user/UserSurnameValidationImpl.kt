package pl.kskarzynski.multiplex.api.validation.user

import arrow.core.EitherNel
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.springframework.stereotype.Service
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserSurnameValidationError
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserSurnameValidationError.InvalidSurnameCharacters
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserSurnameValidationError.SurnameHasTooManyHyphens
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserSurnameValidationError.SurnameNotCapitalized
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserSurnameValidationError.SurnameSecondPartIsNotCapitalized
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserSurnameValidationError.SurnameTooShort
import pl.kskarzynski.multiplex.domain.model.user.UserSurname
import pl.kskarzynski.multiplex.utils.arrow.accumulateErrors
import pl.kskarzynski.multiplex.utils.strings.hyphenCount
import pl.kskarzynski.multiplex.utils.strings.isCapitalized
import pl.kskarzynski.multiplex.utils.strings.secondPartIsCapitalized

@Service
class UserSurnameValidationImpl : UserSurnameValidation {

    override fun validateSurname(surname: String): EitherNel<UserSurnameValidationError, UserSurname> =
        either {
            val invalidCharacters = surname.filter { it !in UserSurname.VALID_CHARACTERS }

            accumulateErrors(
                { ensure(surname.length >= UserSurname.MIN_LENGTH) { SurnameTooShort } },
                { ensure(surname.isCapitalized()) { SurnameNotCapitalized } },
                { ensure(surname.hyphenCount() <= UserSurname.MAX_HYPHEN_COUNT) { SurnameHasTooManyHyphens } },
                { ensure(surname.secondPartIsCapitalized()) { SurnameSecondPartIsNotCapitalized } },
                { ensure(invalidCharacters.isEmpty()) { InvalidSurnameCharacters(invalidCharacters.toList()) } },
            )

            UserSurname(surname)
        }
}
