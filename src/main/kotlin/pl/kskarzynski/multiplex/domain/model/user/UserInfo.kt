package pl.kskarzynski.multiplex.domain.model.user

import arrow.core.Either
import arrow.core.EitherNel
import pl.kskarzynski.multiplex.common.util.extensions.mapErrors

sealed interface UserInfoValidationError {
    data class InvalidName(val error: UserNameValidationError) : UserInfoValidationError
    data class InvalidSurname(val error: UserSurnameValidationError) : UserInfoValidationError
}

data class UserInfo private constructor(
    val name: UserName,
    val surname: UserSurname,
) {
    companion object {
        operator fun invoke(name: String, surname: String): EitherNel<UserInfoValidationError, UserInfo> =
            Either.zipOrAccumulate(
                UserName(name)
                    .mapErrors(UserInfoValidationError::InvalidName),
                UserSurname(surname)
                    .mapErrors(UserInfoValidationError::InvalidSurname),
            ) { validName, validSurname ->
                UserInfo(validName, validSurname)
            }
    }
}
