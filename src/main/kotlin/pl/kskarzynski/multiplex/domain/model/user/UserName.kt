package pl.kskarzynski.multiplex.domain.model.user

import arrow.core.EitherNel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import pl.kskarzynski.multiplex.common.util.extensions.isCapitalized
import pl.kskarzynski.multiplex.domain.model.user.UserNameValidationError.*

sealed interface UserNameValidationError {
    data object NameTooShort : UserNameValidationError
    data object NameNotCapitalized : UserNameValidationError
    data object InvalidNameCharacters : UserNameValidationError
}

@JvmInline
value class UserName private constructor(val value: String) {
    companion object {
        private const val MIN_LENGTH = 3
        private val validCharacters = "^\\p{L}+$".toRegex()

        operator fun invoke(value: String): EitherNel<UserNameValidationError, UserName> =
            either {
                zipOrAccumulate(
                    { ensure(value.length >= MIN_LENGTH) { NameTooShort } },
                    { ensure(value.isCapitalized()) { NameNotCapitalized } },
                    { ensure(value matches validCharacters) { InvalidNameCharacters } },
                ) { _, _, _ ->
                    UserName(value)
                }
            }
    }
}
