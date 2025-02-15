package pl.kskarzynski.multiplex.integration.screening

import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.of
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import pl.kskarzynski.multiplex.common.infra.ktor.CONTENT_TYPE_JSON_UTF_8
import pl.kskarzynski.multiplex.common.test.arbs.bookingId
import pl.kskarzynski.multiplex.common.test.arbs.screeningId
import pl.kskarzynski.multiplex.common.utils.datetime.currentTime
import pl.kskarzynski.multiplex.integration.arbs.expiredBooking
import pl.kskarzynski.multiplex.integration.arbs.screening
import pl.kskarzynski.multiplex.integration.arbs.unconfirmedBooking
import pl.kskarzynski.multiplex.integration.clientWithJson
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.ConfirmedBooking
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingConfirmationErrorDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingConfirmationErrorDto.BookingDoesNotExist
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingConfirmationErrorDto.BookingExpired
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingIdDto
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.one

class BookingConfirmationApiIntegrationTest : ScreeningApiIntegrationTest() {

    init {
        feature("Booking confirmation") {
            scenario("Screening does not exist") {
                testApplication {
                    setupMultiplexApplication()

                    // given:
                    val nonExistentScreeningId = Arb.screeningId().next()
                    val nonExistentBookingId = Arb.bookingId().next()

                    // when:
                    val response = client.post(
                        "/api/screenings/$nonExistentScreeningId/bookings/$nonExistentBookingId/confirm"
                    )

                    // then:
                    expectThat(response.status) isEqualTo HttpStatusCode.NotFound
                }
            }

            scenario("Booking does not exist") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val screening = createScreening()
                    val nonExistentBookingId = Arb.bookingId().next()

                    // when:
                    val response = client.post("/api/screenings/${screening.id}/bookings/$nonExistentBookingId/confirm")

                    // then:
                    expect {
                        that(response) {
                            get { status } isEqualTo HttpStatusCode.BadRequest
                            get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                        }

                        that(response.body<List<BookingConfirmationErrorDto>>()) {
                            hasSize(1)
                            one {
                                isA<BookingDoesNotExist>() and {
                                    get { screeningId } isEqualTo screening.id.value
                                    get { bookingId } isEqualTo nonExistentBookingId.value
                                }
                            }
                        }
                    }
                }
            }

            scenario("Booking is expired") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()
                    val expiredBooking = Arb.expiredBooking(seats = listOf(room.seats.first())).next()
                    val screening = Arb.screening(movie, room).next()
                        .copy(bookings = listOf(expiredBooking))
                        .also { screeningRepository.save(it) }

                    // when:
                    val response = client.post("/api/screenings/${screening.id}/bookings/${expiredBooking.id}/confirm")

                    // then:
                    expect {
                        that(response) {
                            get { status } isEqualTo HttpStatusCode.BadRequest
                            get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                        }

                        that(response.body<List<BookingConfirmationErrorDto>>()) {
                            hasSize(1)
                            one {
                                isA<BookingExpired>() and {
                                    get { screeningId } isEqualTo screening.id.value
                                    get { bookingId } isEqualTo expiredBooking.id.value
                                    get { expiredAt } isEqualTo expiredBooking.expirationTime.value
                                }
                            }
                        }
                    }
                }
            }

            scenario("Valid Booking confirmation") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()
                    val booking = Arb.unconfirmedBooking(
                        expirationTime = Arb.of(fixedClock.currentTime().plusDays(1)),
                        seats = listOf(room.seats.first()),
                    ).next()
                    val screening = Arb.screening(movie, room, bookings = listOf(booking)).next()
                        .also { screeningRepository.save(it) }

                    // when:
                    val response = client.post("/api/screenings/${screening.id}/bookings/${booking.id}/confirm")

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    expectThat(response.body<BookingIdDto>()) {
                        get { bookingId } isEqualTo booking.id.value
                    }

                    val updatedScreening = screeningRepository.findScreening(screening.id)
                    expectThat(updatedScreening).isNotNull()
                        .get { bookings } and {
                            hasSize(screening.bookings.size)
                            one {
                                get { id } isEqualTo booking.id
                                isA<ConfirmedBooking>()
                            }
                        }
                }
            }
        }
    }
}
