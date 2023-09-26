package pl.kskarzynski.multiplex.domain.user.validation

import arrow.core.Either.Right
import arrow.core.EitherNel
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import pl.kskarzynski.multiplex.domain.user.model.UserInfo
import pl.kskarzynski.multiplex.domain.user.model.UserName
import pl.kskarzynski.multiplex.domain.user.model.UserSurname
import pl.kskarzynski.multiplex.domain.user.validation.UserInfoValidationError.*

private object TestUserInfoValidation : UserInfoValidation

class UserInfoValidationSpec : FeatureSpec({

    fun checkValidationResult(
        userInfo: UserInfo,
        block: (result: EitherNel<UserInfoValidationError, UserInfo>) -> Unit,
    ) {
        val validationResult = TestUserInfoValidation.validateUserInfo(userInfo)
        block(validationResult)
    }

    feature("User info validation") {
        scenario("User info is valid") {
            forAll(
                row("Andrzej", "Kowalski"),
                row("Andrzej", "Kowalski-Nowak"),
            ) { name, surname ->
                // given:
                val userInfo = userInfoOf(name, surname)

                // expect:
                checkValidationResult(userInfo) {
                    it shouldBe Right(userInfo)
                }
            }
        }

        scenario("User name is too short") {
            // given:
            val userInfo = userInfoOf(
                name = "Aa",
                surname = "Kowalski",
            )

            // expect:
            checkValidationResult(userInfo) {
                it.isRight() shouldBe false
                it.onLeft { errors ->
                    errors shouldContain NameTooShort
                }
            }
        }

        scenario("User name is not capitalized") {
            // given:
            val userInfo = userInfoOf(
                name = "andrzej",
                surname = "Kowalski",
            )

            // expect:
            checkValidationResult(userInfo) {
                it.isRight() shouldBe false
                it.onLeft { errors ->
                    errors shouldContain NameNotCapitalized
                }
            }
        }

        scenario("User surname is too short") {
            // given:
            val userInfo = userInfoOf(
                name = "Andrzej",
                surname = "Ko",
            )

            // expect:
            checkValidationResult(userInfo) {
                it.isRight() shouldBe false
                it.onLeft { errors ->
                    errors shouldContain SurnameTooShort
                }
            }
        }

        scenario("User surname is not capitalized") {
            // given:
            val userInfo = userInfoOf(
                name = "Andrzej",
                surname = "kowalski",
            )

            // expect:
            checkValidationResult(userInfo) {
                it.isRight() shouldBe false
                it.onLeft { errors ->
                    errors shouldContain SurnameNotCapitalized
                }
            }
        }

        scenario("User surname has too many hyphens") {
            // given:
            val userInfo = userInfoOf(
                name = "Andrzej",
                surname = "Kowalski-Nowak-Aa",
            )

            // expect:
            checkValidationResult(userInfo) {
                it.isRight() shouldBe false
                it.onLeft { errors ->
                    errors shouldContain SurnameHasTooManyHyphens
                }
            }
        }

        scenario("User surname's second part is not capitalized") {
            // given:
            val userInfo = userInfoOf(
                name = "Andrzej",
                surname = "Kowalski-nowak",
            )

            // expect:
            checkValidationResult(userInfo) {
                it.isRight() shouldBe false
                it.onLeft { errors ->
                    errors shouldContain SurnameSecondPartIsNotCapitalized
                }
            }
        }
    }

})

private fun userInfoOf(name: String, surname: String): UserInfo =
    UserInfo(
        name = UserName(name),
        surname = UserSurname(surname),
    )
