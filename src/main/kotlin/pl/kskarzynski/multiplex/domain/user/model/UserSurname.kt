package pl.kskarzynski.multiplex.domain.user.model

import arrow.core.EitherNel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import pl.kskarzynski.multiplex.common.util.extensions.hyphenCount
import pl.kskarzynski.multiplex.common.util.extensions.isCapitalized
import pl.kskarzynski.multiplex.common.util.extensions.secondPartIsCapitalized
import pl.kskarzynski.multiplex.domain.user.model.UserSurnameValidationError.*

sealed interface UserSurnameValidationError {
    data object SurnameTooShort : UserSurnameValidationError
    data object SurnameNotCapitalized : UserSurnameValidationError
    data object SurnameHasTooManyHyphens : UserSurnameValidationError
    data object SurnameSecondPartIsNotCapitalized : UserSurnameValidationError
    data object InvalidSurnameCharacters : UserSurnameValidationError
}

@JvmInline
value class UserSurname private constructor(val value: String) {
    companion object {
        private const val MIN_LENGTH = 3
        private const val MAX_HYPHEN_COUNT = 1

        private val validCharacters = "^[\\p{L}-]+$".toRegex()

        operator fun invoke(value: String): EitherNel<UserSurnameValidationError, UserSurname> =
            either {
                zipOrAccumulate(
                    { ensure(value.length > MIN_LENGTH) { SurnameTooShort } },
                    { ensure(value.isCapitalized()) { SurnameNotCapitalized } },
                    { ensure(value.hyphenCount() <= MAX_HYPHEN_COUNT) { SurnameHasTooManyHyphens } },
                    { ensure(value.secondPartIsCapitalized()) { SurnameSecondPartIsNotCapitalized } },
                    { ensure(value matches validCharacters) { InvalidSurnameCharacters } },
                ) { _, _, _, _, _, ->
                    UserSurname(value)
                }
            }
    }
}
