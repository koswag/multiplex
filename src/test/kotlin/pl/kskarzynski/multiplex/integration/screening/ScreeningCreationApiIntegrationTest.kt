package pl.kskarzynski.multiplex.integration.screening

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
import pl.kskarzynski.multiplex.common.test.arbs.movie
import pl.kskarzynski.multiplex.common.test.arbs.room
import pl.kskarzynski.multiplex.common.utils.datetime.currentTime
import pl.kskarzynski.multiplex.integration.arbs.screening
import pl.kskarzynski.multiplex.integration.clientWithJson
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto.MovieDoesNotExist
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto.PastScreeningTime
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto.RoomDoesNotExist
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.toDto
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo
import strikt.assertions.one

class ScreeningCreationApiIntegrationTest : ScreeningApiIntegrationTest() {

    init {
        feature("Creating a Screening") {
            scenario("There are no Screenings") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()

                    // when:
                    val newScreening = Arb.screening(movie, room, validStartTime).next()
                    val response = client.post("/api/screenings") {
                        contentType(Json)
                        setBody(newScreening.toCreateScreeningDto())
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.Created
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    val screening = response.body<ScreeningDto>()
                    expect {
                        that(screening) {
                            get { startTime } isEqualTo newScreening.startTime.value
                        }
                        that(screening.movie) {
                            get { id } isEqualTo movie.id.value
                            get { title } isEqualTo movie.title.value
                            get { releaseYear } isEqualTo movie.releaseYear.value
                        }
                        that(screening.room) {
                            get { id } isEqualTo room.id.value
                            get { number } isEqualTo room.number.value
                            get { seats } containsExactlyInAnyOrder room.seats.map { it.toDto(emptyList()) }
                        }
                    }
                }
            }

            scenario("Other Screening exists") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()
                    val otherScreening = createScreening(movie, room)

                    // when:
                    val newScreening = Arb.screening(movie, room, validStartTime).next()
                    val response = client.post("/api/screenings") {
                        contentType(Json)
                        setBody(newScreening.toCreateScreeningDto())
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.Created
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    val screening = response.body<ScreeningDto>()
                    expect {
                        that(screening) {
                            get { id } isNotEqualTo otherScreening.id.value
                            get { startTime } isEqualTo newScreening.startTime.value
                        }
                        that(screening.movie) {
                            get { id } isEqualTo movie.id.value
                            get { title } isEqualTo movie.title.value
                            get { releaseYear } isEqualTo movie.releaseYear.value
                        }
                        that(screening.room) {
                            get { id } isEqualTo room.id.value
                            get { number } isEqualTo room.number.value
                            get { seats } containsExactlyInAnyOrder room.seats.map { it.toDto(emptyList()) }
                        }
                    }
                }
            }

            scenario("Movie does not exist") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val nonExistentMovie = Arb.movie().next()
                    val room = createRoom()

                    // when:
                    val newScreening = Arb.screening(nonExistentMovie, room, validStartTime).next()
                    val response = client.post("/api/screenings") {
                        contentType(Json)
                        setBody(newScreening.toCreateScreeningDto())
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.BadRequest
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    expectThat(response.body<List<ScreeningValidationErrorDto>>()) {
                        hasSize(1)
                        one {
                            isA<MovieDoesNotExist>() and {
                                get { movieId } isEqualTo nonExistentMovie.id.value
                            }
                        }
                    }
                }
            }

            scenario("Room does not exist") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val movie = createMovie()
                    val nonExistentRoom = Arb.room().next()

                    // when:
                    val newScreening = Arb.screening(movie, nonExistentRoom, validStartTime).next()
                    val response = client.post("/api/screenings") {
                        contentType(Json)
                        setBody(newScreening.toCreateScreeningDto())
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.BadRequest
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    expectThat(response.body<List<ScreeningValidationErrorDto>>()) {
                        hasSize(1)
                        one {
                            isA<RoomDoesNotExist>() and {
                                get { roomId } isEqualTo nonExistentRoom.id.value
                            }
                        }
                    }
                }
            }

            scenario("Past Screening time") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()
                    val pastScreeningTime = fixedClock.currentTime().minusDays(1)

                    // when:
                    val newScreening = Arb.screening(movie, room, pastScreeningTime).next()
                    val response = client.post("/api/screenings") {
                        contentType(Json)
                        setBody(newScreening.toCreateScreeningDto())
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.BadRequest
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    expectThat(response.body<List<ScreeningValidationErrorDto>>()) {
                        hasSize(1)
                        one {
                            isA<PastScreeningTime>() and {
                                get { screeningTime } isEqualTo pastScreeningTime
                            }
                        }
                    }
                }
            }
        }
    }
}
