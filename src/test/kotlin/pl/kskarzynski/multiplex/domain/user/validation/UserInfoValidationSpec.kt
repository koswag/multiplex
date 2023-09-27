package pl.kskarzynski.multiplex.domain.user.validation

import arrow.core.EitherNel
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.char
import io.kotest.property.checkAll
import pl.kskarzynski.multiplex.domain.user.model.*
import pl.kskarzynski.multiplex.domain.user.model.UserInfoValidationError.InvalidName
import pl.kskarzynski.multiplex.domain.user.model.UserInfoValidationError.InvalidSurname
import pl.kskarzynski.multiplex.domain.user.model.UserNameValidationError.*
import pl.kskarzynski.multiplex.domain.user.model.UserSurnameValidationError.*

class UserInfoValidationSpec : FeatureSpec({

    fun checkValidationResult(
        name: String,
        surname: String,
        block: (result: EitherNel<UserInfoValidationError, UserInfo>) -> Unit,
    ) {
        val userInfo = UserInfo(name, surname)
        block(userInfo)
    }

    feature("User info validation") {
        scenario("User info is valid") {
            forAll(
                row("Andrzej", "Kowalski"),
                row("Andrzej", "Kowalski-Nowak"),
            ) { name, surname ->
                checkValidationResult(name, surname) {
                    it.isRight() shouldBe true
                    it.onRight { userInfo ->
                        userInfo.name shouldBe name
                        userInfo.surname shouldBe surname
                    }
                }
            }
        }

        scenario("User name is too short") {
            checkValidationResult(
                name = "Aa",
                surname = "Kowalski",
            ) {
                it.isRight() shouldBe false
                it.onLeft { errors ->
                    errors shouldContain InvalidName(NameTooShort)
                }
            }
        }

        scenario("User name is not capitalized") {
            checkValidationResult(
                name = "andrzej",
                surname = "Kowalski",
            ) {
                it.isRight() shouldBe false
                it.onLeft { errors ->
                    errors shouldContain InvalidName(NameNotCapitalized)
                }
            }
        }

        scenario("User name contains special characters") {
            checkAll(Arb.invalidNameCharacter()) { specialCharacter ->
                checkValidationResult(
                    name = "Andrzej$specialCharacter",
                    surname = "Kowalski",
                ) {
                    it.isRight() shouldBe false
                    it.onLeft { errors ->
                        errors shouldContain InvalidName(InvalidNameCharacters)
                    }
                }
            }
        }


        scenario("User surname is too short") {
            checkValidationResult(
                name = "Andrzej",
                surname = "Ko",
            ) {
                it.isRight() shouldBe false
                it.onLeft { errors ->
                    errors shouldContain InvalidSurname(SurnameTooShort)
                }
            }
        }

        scenario("User surname is not capitalized") {
            checkValidationResult(
                name = "Andrzej",
                surname = "kowalski",
            ) {
                it.isRight() shouldBe false
                it.onLeft { errors ->
                    errors shouldContain InvalidSurname(SurnameNotCapitalized)
                }
            }
        }

        scenario("User surname has too many hyphens") {
            checkValidationResult(
                name = "Andrzej",
                surname = "Kowalski-Nowak-Aa",
            ) {
                it.isRight() shouldBe false
                it.onLeft { errors ->
                    errors shouldContain InvalidSurname(SurnameHasTooManyHyphens)
                }
            }
        }

        scenario("User surname's second part is not capitalized") {
            checkValidationResult(
                name = "Andrzej",
                surname = "Kowalski-nowak",
            ) {
                it.isRight() shouldBe false
                it.onLeft { errors ->
                    errors shouldContain InvalidSurname(SurnameSecondPartIsNotCapitalized)
                }
            }
        }

        scenario("User surname contains invalid characters") {
            checkAll(Arb.invalidSurnameCharacter()) { specialCharacter ->
                checkValidationResult(
                    name = "Andrzej",
                    surname = "Kowalski$specialCharacter",
                ) {
                    it.isRight() shouldBe false
                    it.onLeft { errors ->
                        errors shouldContain InvalidSurname(InvalidSurnameCharacters)
                    }
                }
            }
        }
    }

})

private fun Arb.Companion.invalidNameCharacter(): Arb<Char> =
    Arb.char(
        Char(32)..Char(47),
        Char(58)..Char(64),
        Char(91)..Char(96),
        Char(123)..Char(126),
    )

private fun Arb.Companion.invalidSurnameCharacter(): Arb<Char> =
    Arb.char(
        Char(32)..Char(44),
        Char(46)..Char(47),
        Char(58)..Char(64),
        Char(91)..Char(96),
        Char(123)..Char(126),
    )
