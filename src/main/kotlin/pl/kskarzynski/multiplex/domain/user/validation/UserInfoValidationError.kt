package pl.kskarzynski.multiplex.domain.user.validation

sealed interface UserInfoValidationError {
    data object NameTooShort : UserInfoValidationError
    data object NameNotCapitalized : UserInfoValidationError
    data object InvalidNameCharacters : UserInfoValidationError

    data object SurnameTooShort : UserInfoValidationError
    data object SurnameNotCapitalized : UserInfoValidationError
    data object SurnameHasTooManyHyphens : UserInfoValidationError
    data object SurnameSecondPartIsNotCapitalized : UserInfoValidationError
    data object InvalidSurnameCharacters : UserInfoValidationError
}
