package pl.kskarzynski.multiplex.domain.model.user

import pl.kskarzynski.multiplex.utils.strings.isCapitalized

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
