package pl.kskarzynski.multiplex.api.validation

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbs.lastName
import io.kotest.property.checkAll
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserSurnameValidationError.InvalidSurnameCharacters
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserSurnameValidationError.SurnameHasTooManyHyphens
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserSurnameValidationError.SurnameNotCapitalized
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserSurnameValidationError.SurnameSecondPartIsNotCapitalized
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserSurnameValidationError.SurnameTooShort
import pl.kskarzynski.multiplex.api.validation.user.UserSurnameValidationImpl
import pl.kskarzynski.multiplex.domain.model.user.UserSurname
import pl.kskarzynski.multiplex.domain.util.insert
import pl.kskarzynski.multiplex.domain.util.shouldBeLeft
import pl.kskarzynski.multiplex.domain.util.shouldBeRight
import pl.kskarzynski.multiplex.domain.util.withPolishCharacter

class UserSurnameValidationSpec : FeatureSpec({

    val validation = UserSurnameValidationImpl()

    feature("User surname validation") {
        scenario("Valid surname") {
            checkAll(Arb.validSurname()) { surname ->
                // when:
                val result = validation.validateSurname(surname)

                // then:
                result.shouldBeRight {
                    it shouldBe UserSurname(surname)
                }
            }
        }

        scenario("Valid surname with Polish character") {
            checkAll(Arb.validSurnameWithPolishCharacter()) { surname ->
                // when:
                val result = validation.validateSurname(surname)

                // then:
                result.shouldBeRight {
                    it shouldBe UserSurname(surname)
                }
            }
        }

        scenario("Valid two-part surname") {
            checkAll(
                Arb.validTwoPartSurname(
                    surname = Arb.validSurname(),
                )
            ) { surname ->
                // when:
                val result = validation.validateSurname(surname)

                // then:
                result.shouldBeRight {
                    it shouldBe UserSurname(surname)
                }
            }
        }

        scenario("Valid two-part surname with Polish characters") {
            checkAll(
                Arb.validTwoPartSurname(
                    surname = Arb.validSurnameWithPolishCharacter(),
                )
            ) { surname ->
                // when:
                val result = validation.validateSurname(surname)

                // then:
                result.shouldBeRight {
                    it shouldBe UserSurname(surname)
                }
            }
        }

        scenario("Surname is too short") {
            checkAll(Arb.tooShortSurname()) { surname ->
                // when:
                val result = validation.validateSurname(surname)

                // then:
                result.shouldBeLeft { errors ->
                    errors shouldContain SurnameTooShort
                }
            }
        }

        scenario("Surname is not capitalized") {
            checkAll(Arb.nonCapitalizedSurname()) { surname ->
                // when:
                val result = validation.validateSurname(surname)

                // then:
                result.shouldBeLeft { errors ->
                    errors shouldContain SurnameNotCapitalized
                }
            }
        }

        scenario("Surname contains too many hyphens") {
            checkAll(Arb.surnameWithTooManyHyphens()) { surname ->
                // when:
                val result = validation.validateSurname(surname)

                // then:
                result.shouldBeLeft { errors ->
                    errors shouldContain SurnameHasTooManyHyphens
                }
            }
        }

        scenario("Second part of two-part surname is not capitalized") {
            checkAll(Arb.twoPartSurnameWithNonCapitalizedSecondPart()) { surname ->
                // when:
                val result = validation.validateSurname(surname)

                // then:
                result.shouldBeLeft { errors ->
                    errors shouldContain SurnameSecondPartIsNotCapitalized
                }
            }
        }

        scenario("Surname contains invalid characters") {
            checkAll(Arb.surnameWithIllegalCharacter()) { surname ->
                // when:
                val result = validation.validateSurname(surname)

                // then:
                val illegalChars = surname.filter { it !in UserSurname.VALID_CHARACTERS }.toList()

                result.shouldBeLeft { errors ->
                    errors shouldContain InvalidSurnameCharacters(illegalChars)
                }
            }
        }
    }

})

private fun Arb.Companion.validSurname(): Arb<String> =
    Arb.lastName()
        .map { it.name.lowercase().replaceFirstChar(Char::uppercase) }
        .filter { it.length >= UserSurname.MIN_LENGTH }

private fun Arb.Companion.validSurnameWithPolishCharacter(): Arb<String> =
    Arb.validSurname()
        .withPolishCharacter(probability = 0.2)

private fun Arb.Companion.validTwoPartSurname(surname: Arb<String>): Arb<String> =
    arbitrary {
        val firstPart = surname.bind()
        val secondPart = surname.bind()

        "$firstPart-$secondPart"
    }

private fun Arb.Companion.tooShortSurname(): Arb<String> =
    arbitrary {
        val surname = Arb.validSurname().bind()
        val shortSurnameLength = Arb.int(1..<UserSurname.MIN_LENGTH).bind()

        surname.take(shortSurnameLength)
    }

private fun Arb.Companion.nonCapitalizedSurname(): Arb<String> =
    Arb.validSurname()
        .map { it.lowercase() }

private fun Arb.Companion.surnameWithTooManyHyphens(): Arb<String> =
    arbitrary {
        val hyphenCount =
            Arb.int(
                min = UserSurname.MAX_HYPHEN_COUNT + 1,
                max = 10,
            ).bind()

        val surnameParts = List(hyphenCount + 1) { Arb.validSurname().bind() }
        surnameParts.joinToString(separator = "-")
    }

private fun Arb.Companion.twoPartSurnameWithNonCapitalizedSecondPart(): Arb<String> =
    arbitrary {
        val twoPartSurname =
            Arb.validTwoPartSurname(
                surname = Arb.validSurname(),
            ).bind()

        val (firstPart, secondPart) = twoPartSurname.split("-")
        "$firstPart-${secondPart.lowercase()}"
    }

private fun Arb.Companion.surnameWithIllegalCharacter(): Arb<String> =
    arbitrary {
        val surname = Arb.validSurname().bind()
        val illegalChar = Arb.char().filter { it !in UserSurname.VALID_CHARACTERS }.bind()

        val illegalCharOffset = Arb.int(1..<surname.length).bind()
        surname.insert(illegalCharOffset, illegalChar)
    }
