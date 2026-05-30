package pl.kskarzynski.multiplex.screenings.domain.model.booking

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import pl.kskarzynski.multiplex.common.test.arrow.isLeft
import pl.kskarzynski.multiplex.common.test.arrow.isRight
import pl.kskarzynski.multiplex.common.utils.datetime.minus
import pl.kskarzynski.multiplex.common.utils.datetime.plus
import pl.kskarzynski.multiplex.screenings.domain.util.DEFAULT_EXPIRATION_TIME
import pl.kskarzynski.multiplex.screenings.domain.util.unconfirmedBooking
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import kotlin.time.Duration.Companion.seconds

class BookingTest : FeatureSpec({

    feature("Booking confirmation") {
        scenario("Before expiration") {
            // given:
            val bookingExpirationTime = DEFAULT_EXPIRATION_TIME
            val booking = unconfirmedBooking(expirationTime = bookingExpirationTime)

            checkAll(Arb.int(1..10_000_000)) { timeOffsetInSeconds ->
                val currentTime = bookingExpirationTime.value - timeOffsetInSeconds.seconds
                val confirmationResult = booking.confirm(currentTime)

                expectThat(confirmationResult).isRight() and {
                    get { userInfo } isEqualTo booking.userInfo
                    get { tickets } isEqualTo booking.tickets
                    get { bookingTime } isEqualTo booking.bookingTime
                    get { expirationTime } isEqualTo booking.expirationTime
                    get { totalPrice } isEqualTo booking.totalPrice
                    get { confirmationTime.value } isEqualTo currentTime
                }
            }
        }

        scenario("After expiration") {
            // given:
            val bookingExpirationTime = DEFAULT_EXPIRATION_TIME
            val booking = unconfirmedBooking(expirationTime = bookingExpirationTime)

            checkAll(Arb.int(0..10_000_000)) { timeOffsetInSeconds ->
                val currentTime = bookingExpirationTime.value + timeOffsetInSeconds.seconds
                val confirmationResult = booking.confirm(currentTime)

                expectThat(confirmationResult)
                    .isLeft()
                    .isA<BookingConfirmationError.BookingExpired>()
                    .get { expiredAt } isEqualTo bookingExpirationTime
            }
        }
    }
})
