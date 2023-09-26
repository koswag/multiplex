package pl.kskarzynski.multiplex.domain.user.validation

import arrow.core.EitherNel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import pl.kskarzynski.multiplex.domain.user.model.UserInfo
import pl.kskarzynski.multiplex.domain.user.validation.UserInfoValidationError.*

interface UserInfoValidation {

    companion object {
        private const val MIN_NAME_LENGTH = 3
        private const val MIN_SURNAME_LENGTH = 3
        private const val MAX_HYPHEN_COUNT = 1
    }

    fun validateUserInfo(userInfo: UserInfo): EitherNel<UserInfoValidationError, UserInfo> =
        either {
            val name = userInfo.name.value
            val surname = userInfo.surname.value

            zipOrAccumulate(
                { ensure(name.length >= MIN_NAME_LENGTH) { NameTooShort } },
                { ensure(name.isCapitalized()) { NameNotCapitalized } },

                { ensure(surname.length > MIN_SURNAME_LENGTH) { SurnameTooShort } },
                { ensure(surname.isCapitalized()) { SurnameNotCapitalized } },
                { ensure(surname.hyphenCount() <= MAX_HYPHEN_COUNT) { SurnameHasTooManyHyphens } },
                { ensure(surname.secondPartIsCapitalized()) { SurnameSecondPartIsNotCapitalized } },
            ) { _, _, _, _, _, _ ->
                userInfo
            }
        }

    private fun String.isCapitalized(): Boolean =
        firstOrNull()
            ?.isUpperCase() == true

    private fun String.hyphenCount(): Int =
        count { it == '-' }

    private fun String.secondPartIsCapitalized(): Boolean =
        hyphenCount() == 0
            || split('-')[1].isCapitalized()

}
