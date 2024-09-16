package pl.kskarzynski.multiplex.api.validation

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbs.firstName
import io.kotest.property.checkAll
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserNameValidationError.InvalidNameCharacters
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserNameValidationError.NameNotCapitalized
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.UserNameValidationError.NameTooShort
import pl.kskarzynski.multiplex.api.validation.user.UserNameValidationImpl
import pl.kskarzynski.multiplex.domain.model.user.UserName
import pl.kskarzynski.multiplex.domain.util.insert
import pl.kskarzynski.multiplex.domain.util.polishCharacter
import pl.kskarzynski.multiplex.domain.util.shouldBeLeft
import pl.kskarzynski.multiplex.domain.util.shouldBeRight

class UserNameValidationSpec : FeatureSpec({

    val userNameValidation = UserNameValidationImpl()

    feature("Name validation") {
        scenario("Name is valid") {
            checkAll(Arb.validUserName()) { userName ->
                // when:
                val result = userNameValidation.validateName(userName)

                // then:
                result.shouldBeRight { UserName(userName) }
            }
        }

        scenario("Name is too short") {
            checkAll(Arb.tooShortUserName()) { userName ->
                // when:
                val result = userNameValidation.validateName(userName)

                // then:
                result.shouldBeLeft { errors ->
                    errors.shouldContainExactly(NameTooShort)
                }
            }
        }

        scenario("Name is not capitalized long") {
            checkAll(Arb.notCapitalizedUserName()) { userName ->
                // when:
                val result = userNameValidation.validateName(userName)

                // then:
                result.shouldBeLeft { errors ->
                    errors.shouldContainExactly(NameNotCapitalized)
                }
            }
        }

        scenario("Name contains invalid characters") {
            checkAll(Arb.userNameWithInvalidCharacters()) { userName ->
                // when:
                val result = userNameValidation.validateName(userName)

                // then:
                val illegalChars = userName.filter { it !in UserName.VALID_CHARACTERS }.toList()

                result.shouldBeLeft { errors ->
                    errors.shouldContainExactly(InvalidNameCharacters(illegalChars))
                }
            }
        }
    }

})

private fun Arb.Companion.validUserName(): Arb<String> =
    arbitrary {
        val name = Arb.firstName()
            .map { it.name }
            .filter { it.length > UserName.MIN_LENGTH }
            .bind()

        val polishCharacter = Arb.polishCharacter().orNull(nullProbability = 0.2).bind()
        val polishCharacterOffset = Arb.int(1..<name.length).bind()

        if (polishCharacter != null) {
            name.insert(polishCharacterOffset, polishCharacter)
        } else {
            name
        }
    }

private fun Arb.Companion.tooShortUserName(): Arb<String> =
    arbitrary {
        val name = Arb.validUserName().bind()
        val resultLength = Arb.int(1..<UserName.MIN_LENGTH).bind()

        name.take(resultLength)
    }

private fun Arb.Companion.notCapitalizedUserName(): Arb<String> =
    Arb.validUserName()
        .map { it.lowercase() }

private fun Arb.Companion.userNameWithInvalidCharacters(): Arb<String> =
    arbitrary {
        val name = Arb.validUserName().bind()
        val invalidCharacter = Arb.invalidNameCharacter().bind()
        val invalidCharacterOffset = Arb.int(1..<name.length).bind()

        name.insert(invalidCharacterOffset, invalidCharacter)
    }

private fun Arb.Companion.invalidNameCharacter(): Arb<Char> =
    Arb.char(
        Char(32)..Char(47),
        Char(58)..Char(64),
        Char(91)..Char(96),
        Char(123)..Char(126),
    )


