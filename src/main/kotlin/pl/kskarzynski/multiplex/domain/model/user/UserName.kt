package pl.kskarzynski.multiplex.domain.model.user

import pl.kskarzynski.multiplex.common.util.extensions.isCapitalized

sealed interface UserNameValidationError {
    data object NameTooShort : UserNameValidationError
    data object NameNotCapitalized : UserNameValidationError
    data object InvalidNameCharacters : UserNameValidationError
}

@JvmInline
value class UserName(val value: String) {
    init {
        require(value.length >= MIN_LENGTH) { "Username has to have at least $MIN_LENGTH characters: $value" }
        require(value.isCapitalized()) { "Username has to be capitalized: $value" }
        require(value matches VALID_CHARACTERS) { "Username contains invalid characters: $value" }
    }

    companion object {
        const val MIN_LENGTH = 3
        val VALID_CHARACTERS = "^\\p{L}+$".toRegex()
    }
}
