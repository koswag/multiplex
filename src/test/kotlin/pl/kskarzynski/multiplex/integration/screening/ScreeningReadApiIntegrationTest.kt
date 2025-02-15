package pl.kskarzynski.multiplex.integration.screening

import io.kotest.property.Arb
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.next
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import pl.kskarzynski.multiplex.common.infra.ktor.CONTENT_TYPE_JSON_UTF_8
import pl.kskarzynski.multiplex.common.infra.misc.PageDto
import pl.kskarzynski.multiplex.common.test.arbs.movieId
import pl.kskarzynski.multiplex.common.test.arbs.screeningId
import pl.kskarzynski.multiplex.integration.clientWithJson
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningListItemDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.toDto
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class ScreeningReadApiIntegrationTest : ScreeningApiIntegrationTest() {

    init {
        feature("Getting a Screening") {
            scenario("Screening does not exist") {
                testApplication {
                    // given:
                    setupMultiplexApplication()

                    // when:
                    val nonExistentScreeningId = Arb.screeningId().next()
                    val response = client.get("/api/screenings/$nonExistentScreeningId")

                    // then:
                    expectThat(response.status) isEqualTo HttpStatusCode.NotFound
                }
            }

            scenario("Screening has no bookings") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()
                    val existentScreening = createScreening(movie, room)

                    // when:
                    val response = client.get("/api/screenings/${existentScreening.id}")

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    val screening = response.body<ScreeningDto>()
                    expect {
                        that(screening) {
                            get { id } isEqualTo existentScreening.id.value
                            get { startTime } isEqualTo existentScreening.startTime.value
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
        }

        feature("Getting all Screenings by Movie") {
            scenario("Movie does not exist") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val nonExistentMovieId = Arb.movieId().next()

                    // when:
                    val response = client.get("/api/movies/$nonExistentMovieId/screenings")

                    // then:
                    expectThat(response.status) isEqualTo HttpStatusCode.NotFound
                }
            }

            scenario("Movie has no Screenings") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val movie = createMovie()

                    // when:
                    val response = client.get("/api/movies/${movie.id}/screenings") {
                        withQueryParams(
                            "page" to 1,
                            "size" to 5,
                        )
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    val screenings = response.body<PageDto<ScreeningListItemDto>>()
                    expectThat(screenings) {
                        get { content }.isEmpty()
                        get { pageNumber } isEqualTo 1
                        get { pageSize } isEqualTo 5
                        get { totalCount } isEqualTo 0
                    }
                }
            }

            scenario(" Movie has only past Screenings") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val currentTime = Arb.localDateTime().next()

                    val movie = createMovie()
                    val room = createRoom()
                    createScreening(movie, room, startTime = currentTime.minusDays(2))
                    createScreening(movie, room, startTime = currentTime.minusDays(4))
                    createScreening(movie, room, startTime = currentTime.minusDays(10))

                    // when:
                    val response = client.get("/api/movies/${movie.id}/screenings") {
                        withQueryParams(
                            "page" to 1,
                            "size" to 5,
                            "from" to currentTime.format(ISO_LOCAL_DATE_TIME),
                        )
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    val screenings = response.body<PageDto<ScreeningListItemDto>>()
                    expectThat(screenings) {
                        get { content }.isEmpty()
                        get { pageNumber } isEqualTo 1
                        get { pageSize } isEqualTo 5
                        get { totalCount } isEqualTo 0
                    }
                }
            }

            scenario("Movie has future Screenings") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val currentTime = Arb.localDateTime().next()

                    val movie = createMovie()
                    val room = createRoom()
                    createScreening(movie, room, startTime = currentTime.minusDays(2))
                    createScreening(movie, room, startTime = currentTime.minusDays(4))
                    createScreening(movie, room, startTime = currentTime.minusDays(10))

                    val futureScreenings = listOf(
                        createScreening(movie, room, startTime = currentTime.plusDays(1)),
                        createScreening(movie, room, startTime = currentTime.plusDays(9)),
                        createScreening(movie, room, startTime = currentTime.plusDays(5)),
                    )

                    // when:
                    val response = client.get("/api/movies/${movie.id}/screenings") {
                        withQueryParams(
                            "page" to 1,
                            "size" to 5,
                            "from" to currentTime.format(ISO_LOCAL_DATE_TIME),
                        )
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    val screenings = response.body<PageDto<ScreeningListItemDto>>()
                    expect {
                        that(screenings) {
                            get { pageNumber } isEqualTo 1
                            get { pageSize } isEqualTo 5
                            get { totalCount } isEqualTo 3
                        }
                        that(screenings.content) {
                            containsExactly(futureScreenings.map { it.toListItemDto() }.sortedBy { it.startTime })
                        }
                    }
                }
            }

            scenario("Movie has Screenings in given period") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val periodStart = Arb.localDateTime().next()
                    val periodEnd = periodStart.plusDays(3)

                    val movie = createMovie()
                    val room = createRoom()
                    createScreening(movie, room, startTime = periodStart.minusDays(2))
                    createScreening(movie, room, startTime = periodStart.minusDays(4))
                    createScreening(movie, room, startTime = periodStart.minusDays(10))
                    createScreening(movie, room, startTime = periodEnd.plusDays(2))
                    createScreening(movie, room, startTime = periodEnd.plusDays(4))
                    createScreening(movie, room, startTime = periodEnd.plusDays(10))

                    val screeningsInPeriod = listOf(
                        createScreening(movie, room, startTime = periodStart.plusDays(2)),
                        createScreening(movie, room, startTime = periodStart.plusDays(1)),
                    )

                    // when:
                    val response = client.get("/api/movies/${movie.id}/screenings") {
                        withQueryParams(
                            "page" to 1,
                            "size" to 5,
                            "from" to periodStart.format(ISO_LOCAL_DATE_TIME),
                            "to" to periodEnd.format(ISO_LOCAL_DATE_TIME),
                        )
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    val screenings = response.body<PageDto<ScreeningListItemDto>>()
                    expectThat(screenings) {
                        get { content }.containsExactly(
                            screeningsInPeriod.map { it.toListItemDto() }.sortedBy { it.startTime }
                        )
                        get { pageNumber } isEqualTo 1
                        get { pageSize } isEqualTo 5
                        get { totalCount } isEqualTo 2
                    }
                }
            }
        }
    }
}
