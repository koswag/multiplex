package pl.kskarzynski.multiplex.domain.model.user

import pl.kskarzynski.multiplex.utils.strings.hyphenCount
import pl.kskarzynski.multiplex.utils.strings.isCapitalized
import pl.kskarzynski.multiplex.utils.strings.secondPartIsCapitalized

@JvmInline
value class UserSurname(val value: String) {
    init {
        require(value.length >= MIN_LENGTH) { "Surname has to have at least $MIN_LENGTH characters: $value" }
        require(value.isCapitalized()) { "Surname has to be capitalized: $value" }
        require(value.hyphenCount() <= MAX_HYPHEN_COUNT) { "Surname can have at most $MAX_HYPHEN_COUNT hyphens: $value" }
        require(value.secondPartIsCapitalized()) { "Surname's second part have to be capitalized: $value" }
        require(value.all { it in VALID_CHARACTERS }) { "Surname contains invalid characters: $value" }
    }

    companion object {
        const val MIN_LENGTH = 3
        const val MAX_HYPHEN_COUNT = 1
        val VALID_CHARACTERS = LOWERCASE_CHARACTERS + UPPERCASE_CHARACTERS + POLISH_CHARACTERS + HYPHEN
    }
}
