package pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.raise.either
import arrow.core.raise.ensure
import kotlinx.serialization.Serializable
import pl.kskarzynski.multiplex.common.utils.arrow.accumulateErrors
import pl.kskarzynski.multiplex.common.utils.strings.hyphenCount
import pl.kskarzynski.multiplex.common.utils.strings.isCapitalized
import pl.kskarzynski.multiplex.common.utils.strings.secondPartIsCapitalized
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserInfo
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserName
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserSurname
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto.UserNameContainsIllegalCharacters
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto.UserNameNotCapitalized
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto.UserNameTooShort
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto.UserSurnameContainsIllegalCharacters
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto.UserSurnameHasTooManyHyphens
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto.UserSurnameNotCapitalized
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto.UserSurnameSecondPartIsNotCapitalized
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto.UserSurnameTooShort

@Serializable
data class UserInfoDto(
    val name: String,
    val surname: String,
)

fun UserInfoDto.toDomain(): EitherNel<BookingValidationErrorDto, UserInfo> =
    Either.zipOrAccumulate(
        validateUserName(name),
        validateUserSurname(surname),
    ) { validName, validSurname ->
        UserInfo(validName, validSurname)
    }

private fun validateUserName(name: String): EitherNel<BookingValidationErrorDto, UserName> =
    either {
        val illegalChars = name.toList().filter { it !in UserName.VALID_CHARACTERS }

        accumulateErrors(
            { ensure(name.length >= UserName.MIN_LENGTH) { UserNameTooShort(name) } },
            { ensure(name.isCapitalized()) { UserNameNotCapitalized(name) } },
            { ensure(illegalChars.isEmpty()) { UserNameContainsIllegalCharacters(name, illegalChars) } }
        )

        UserName(name)
    }

private fun validateUserSurname(surname: String): EitherNel<BookingValidationErrorDto, UserSurname> =
    either {
        val illegalCharacters = surname.toList().filter { it !in UserName.VALID_CHARACTERS }

        accumulateErrors(
            { ensure(surname.length >= UserName.MIN_LENGTH) { UserSurnameTooShort(surname) } },
            { ensure(surname.isCapitalized()) { UserSurnameNotCapitalized(surname) } },
            { ensure(surname.hyphenCount() <= UserSurname.MAX_HYPHEN_COUNT) { UserSurnameHasTooManyHyphens(surname) } },
            { ensure(surname.secondPartIsCapitalized()) { UserSurnameSecondPartIsNotCapitalized(surname) } },
            { ensure(illegalCharacters.isEmpty()) { UserSurnameContainsIllegalCharacters(surname, illegalCharacters) } },
        )

        UserSurname(surname)
    }
