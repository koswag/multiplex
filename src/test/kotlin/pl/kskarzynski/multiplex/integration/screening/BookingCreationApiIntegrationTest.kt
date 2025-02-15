package pl.kskarzynski.multiplex.integration.screening

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import pl.kskarzynski.multiplex.common.infra.ktor.CONTENT_TYPE_JSON_UTF_8
import pl.kskarzynski.multiplex.common.test.arbs.screeningId
import pl.kskarzynski.multiplex.common.test.arbs.seat
import pl.kskarzynski.multiplex.integration.arbs.userInfo
import pl.kskarzynski.multiplex.integration.clientWithJson
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserInfo
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingErrorDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingErrorDto.SeatDoesNotExist
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingIdDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingRequestDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingSeatDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto.IllegalSeatNumber
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.TicketDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.TicketTypeDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.UserInfoDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.toDto
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatRow
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.one

class BookingCreationApiIntegrationTest : ScreeningApiIntegrationTest() {

    init {
        feature("Booking creation") {
            scenario("Screening does not exist") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val nonExistentScreeningId = Arb.screeningId().next()

                    // when:
                    val bookingRequest = bookingRequestDto(Arb.seat().next())
                    val response = client.post("/api/screenings/$nonExistentScreeningId/bookings") {
                        contentType(Json)
                        setBody(bookingRequest)
                    }

                    // then:
                    expectThat(response.status) isEqualTo HttpStatusCode.NotFound
                }
            }

            scenario("Valid Booking") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()
                    val screening = createScreening(movie, room)

                    val userInfo = Arb.userInfo().next()

                    // when:
                    val bookingRequest = bookingRequestDto(userInfo.toDto(), room.seats.first())
                    val response = client.post("/api/screenings/${screening.id}/bookings") {
                        contentType(Json)
                        setBody(bookingRequest)
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    val createdBookingId = response.body<BookingIdDto>()
                    val updatedScreening = screeningRepository.findScreening(screening.id)
                    expectThat(updatedScreening).isNotNull().get { bookings } and {
                        hasSize(1)
                        one {
                            get { id.value } isEqualTo createdBookingId.bookingId
                        }
                    }
                }
            }

            scenario("Booking validation failure") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()
                    val screening = createScreening(movie, room)

                    val invalidSeat = BookingSeatDto(1, -1)

                    // when:
                    val bookingRequest = BookingRequestDto(
                        userInfo = userInfoDto(),
                        tickets = nonEmptyListOf(
                            TicketDto(TicketTypeDto.ADULT, invalidSeat),
                        )
                    )
                    val response = client.post("/api/screenings/${screening.id}/bookings") {
                        contentType(Json)
                        setBody(bookingRequest)
                    }

                    // then:
                    expect {
                        that(response) {
                            get { status } isEqualTo HttpStatusCode.BadRequest
                            get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                        }

                        that(response.body<List<BookingValidationErrorDto>>()) {
                            hasSize(1)
                            one {
                                isA<IllegalSeatNumber>() and {
                                    get { number } isEqualTo invalidSeat.number
                                }
                            }
                        }
                    }
                }
            }

            scenario("Booking failure") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()
                    val screening = createScreening(movie, room)

                    val lastSeat = room.seats.last()
                    val nonExistentSeat = lastSeat.copy(row = SeatRow(lastSeat.row.value + 1))

                    // when:
                    val bookingRequest = bookingRequestDto(nonExistentSeat)
                    val response = client.post("/api/screenings/${screening.id}/bookings") {
                        contentType(Json)
                        setBody(bookingRequest)
                    }

                    // then:
                    expect {
                        that(response) {
                            get { status } isEqualTo HttpStatusCode.Conflict
                            get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                        }

                        that(response.body<List<BookingErrorDto>>()) {
                            hasSize(1)
                            one {
                                isA<SeatDoesNotExist>() and {
                                    get { seat.row } isEqualTo nonExistentSeat.row.value
                                    get { seat.number } isEqualTo nonExistentSeat.number.value
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun bookingRequestDto(first: Seat, vararg rest: Seat) =
    bookingRequestDto(seats = nonEmptyListOf(first, *rest))

private fun bookingRequestDto(userInfo: UserInfoDto, first: Seat, vararg rest: Seat) =
    bookingRequestDto(userInfo, seats = nonEmptyListOf(first, *rest))

private fun bookingRequestDto(
    userInfo: UserInfoDto = userInfoDto(),
    seats: NonEmptyList<Seat>,
) = BookingRequestDto(
    userInfo = userInfo,
    tickets = seats.map { seat ->
        TicketDto(
            type = TicketTypeDto.ADULT,
            seat = seat.toDto(),
        )
    }
)

private fun userInfoDto(): UserInfoDto {
    val userInfo = Arb.userInfo().next()
    return userInfo.toDto()
}

private fun UserInfo.toDto() =
    UserInfoDto(
        name = name.value,
        surname = surname.value,
    )
