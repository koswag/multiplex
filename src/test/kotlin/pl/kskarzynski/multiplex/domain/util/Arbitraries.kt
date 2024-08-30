package pl.kskarzynski.multiplex.domain.util

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.orNull
import pl.kskarzynski.multiplex.domain.model.user.POLISH_CHARACTERS

fun Arb.Companion.polishCharacter(): Arb<Char> =
    Arb.of(POLISH_CHARACTERS)
        .map { it.lowercaseChar() }

fun Arb<String>.withPolishCharacter(probability: Double): Arb<String> =
    this.flatMap { it.withPolishCharacter(probability) }

fun String.withPolishCharacter(probability: Double): Arb<String> =
    arbitrary {
        val string = this@withPolishCharacter
        val polishCharacter = Arb.polishCharacter().orNull(probability).bind()

        if (polishCharacter != null) {
            val polishCharacterOffset = Arb.int(1..<string.length).bind()
            string.insert(polishCharacterOffset, polishCharacter)
        } else {
            string
        }
    }
