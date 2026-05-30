package pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket

import io.kotest.core.spec.style.FeatureSpec
import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.TicketType.*
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.math.BigDecimal

class TicketTest : FeatureSpec({

    val expectedAdultTicketPrice = BigDecimal(50)
    val expectedStudentTicketPrice = BigDecimal(25)
    val expectedChildTicketPrice = BigDecimal("12.50")

    feature("Base ticket price") {
        scenario("Adult ticket") {
            expectThat(ADULT.price.value) isEqualTo expectedAdultTicketPrice
        }

        scenario("Student ticket") {
            expectThat(STUDENT.price.value) isEqualTo expectedStudentTicketPrice
        }

        scenario("Child ticket") {
            expectThat(CHILD.price.value) isEqualTo expectedChildTicketPrice
        }
    }
})
