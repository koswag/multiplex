package pl.kskarzynski.multiplex.screenings.domain.model.booking.user

import pl.kskarzynski.multiplex.common.utils.strings.isCapitalized
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserValidationConstants.LOWERCASE_CHARACTERS
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserValidationConstants.POLISH_CHARACTERS
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserValidationConstants.UPPERCASE_CHARACTERS

@JvmInline
value class UserName(val value: String) {
    init {
        require(value.length >= MIN_LENGTH) { "Username has to have at least $MIN_LENGTH characters: $value" }
        require(value.isCapitalized()) { "Username has to be capitalized: $value" }
        require(value.all { it in VALID_CHARACTERS }) { "Username contains invalid characters: $value" }
    }

    companion object {
        const val MIN_LENGTH = 3
        val VALID_CHARACTERS = LOWERCASE_CHARACTERS + UPPERCASE_CHARACTERS + POLISH_CHARACTERS
    }
}
