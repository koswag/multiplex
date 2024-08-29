package pl.kskarzynski.multiplex.api.validation

import arrow.core.EitherNel
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.springframework.stereotype.Service
import pl.kskarzynski.multiplex.common.util.extensions.accumulateErrors
import pl.kskarzynski.multiplex.common.util.extensions.hyphenCount
import pl.kskarzynski.multiplex.common.util.extensions.isCapitalized
import pl.kskarzynski.multiplex.common.util.extensions.secondPartIsCapitalized
import pl.kskarzynski.multiplex.domain.model.user.UserSurname
import pl.kskarzynski.multiplex.domain.model.user.UserSurnameValidationError
import pl.kskarzynski.multiplex.domain.model.user.UserSurnameValidationError.InvalidSurnameCharacters
import pl.kskarzynski.multiplex.domain.model.user.UserSurnameValidationError.SurnameHasTooManyHyphens
import pl.kskarzynski.multiplex.domain.model.user.UserSurnameValidationError.SurnameNotCapitalized
import pl.kskarzynski.multiplex.domain.model.user.UserSurnameValidationError.SurnameSecondPartIsNotCapitalized
import pl.kskarzynski.multiplex.domain.model.user.UserSurnameValidationError.SurnameTooShort

@Service
class UserSurnameValidationImpl : UserSurnameValidation {

    override fun validateSurname(surname: String): EitherNel<UserSurnameValidationError, UserSurname> =
        either {
            accumulateErrors(
                { ensure(surname.length >= UserSurname.MIN_LENGTH) { SurnameTooShort } },
                { ensure(surname.isCapitalized()) { SurnameNotCapitalized } },
                { ensure(surname.hyphenCount() <= UserSurname.MAX_HYPHEN_COUNT) { SurnameHasTooManyHyphens } },
                { ensure(surname.secondPartIsCapitalized()) { SurnameSecondPartIsNotCapitalized } },
                { ensure(surname.all { it in UserSurname.VALID_CHARACTERS }) { InvalidSurnameCharacters } },
            )

            UserSurname(surname)
        }
}