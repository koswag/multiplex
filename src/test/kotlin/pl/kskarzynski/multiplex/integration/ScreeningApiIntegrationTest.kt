package pl.kskarzynski.multiplex.integration

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.koin.KoinExtension
import io.kotest.property.Arb
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.next
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.TestApplicationBuilder
import io.ktor.server.testing.testApplication
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import org.koin.test.KoinTest
import org.koin.test.inject
import pl.kskarzynski.multiplex.CommonModule
import pl.kskarzynski.multiplex.auth.AuthenticationModule.authModule
import pl.kskarzynski.multiplex.common.infra.ktor.CONTENT_TYPE_JSON_UTF_8
import pl.kskarzynski.multiplex.common.infra.misc.PageDto
import pl.kskarzynski.multiplex.common.test.arbs.movie
import pl.kskarzynski.multiplex.common.test.arbs.movieId
import pl.kskarzynski.multiplex.common.test.arbs.room
import pl.kskarzynski.multiplex.common.test.arbs.screeningId
import pl.kskarzynski.multiplex.common.test.exposed.initializeDatabase
import pl.kskarzynski.multiplex.common.test.testcontainers.installPostgresContainer
import pl.kskarzynski.multiplex.configureApplication
import pl.kskarzynski.multiplex.integration.arbs.screening
import pl.kskarzynski.multiplex.movies.service.config.MovieModule
import pl.kskarzynski.multiplex.movies.service.data.MovieRepository
import pl.kskarzynski.multiplex.movies.service.data.table.MovieTable
import pl.kskarzynski.multiplex.rooms.service.config.RoomModule
import pl.kskarzynski.multiplex.rooms.service.data.RoomRepository
import pl.kskarzynski.multiplex.rooms.service.data.table.RoomSeatTable
import pl.kskarzynski.multiplex.rooms.service.data.table.RoomTable
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.port.data.ScreeningRepository
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.BookingTable
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.ScreeningTable
import pl.kskarzynski.multiplex.screenings.infra.config.ScreeningModule
import pl.kskarzynski.multiplex.screenings.infra.rest.ScreeningRestModule.screeningModule
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.CreateScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningListItemDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningListItemRoomDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.toDto
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.room.Room
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class ScreeningApiIntegrationTest : KoinTest, FeatureSpec() {

    val screeningRepository by inject<ScreeningRepository>()
    val roomRepository by inject<RoomRepository>()
    val movieRepository by inject<MovieRepository>()

    init {
        isolationMode = IsolationMode.InstancePerLeaf
        extensions(
            KoinExtension(
                listOf(
                    ScreeningModule,
                    MovieModule,
                    RoomModule,
                    CommonModule,
                ),
            ),
        )

        beforeSpec {
            val datasource = installPostgresContainer()
            initializeDatabase(datasource,
                RoomTable,
                RoomSeatTable,
                MovieTable,
                ScreeningTable,
                BookingTable,
            )
        }

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

            scenario("Movie has only past Screenings") {
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

        feature("Creating a Screening") {
            scenario("There are no Screenings") {
                testApplication {
                    setupMultiplexApplication()
                    val client = clientWithJson()

                    // given:
                    val movie = createMovie()
                    val room = createRoom()

                    // when:
                    val newScreening = Arb.screening(movie, room).next()
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
                            get { id } isEqualTo newScreening.id.value
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

            scenario("Movie does not exist") {}
            scenario("Room does not exist") {}
        }
    }

    private suspend fun createScreening(movie: Movie, room: Room, startTime: LocalDateTime? = null): Screening =
        Arb.screening(movie, room, startTime).next()
            .also { screeningRepository.save(it) }

    private suspend fun createMovie(): Movie =
        Arb.movie().next()
            .also { movieRepository.save(it) }

    private suspend fun createRoom(): Room =
        Arb.room().next()
            .also { roomRepository.save(it) }
}

private fun TestApplicationBuilder.setupMultiplexApplication() {
    application {
        configureApplication()
        authModule()
        screeningModule()
    }
}

private fun Screening.toCreateScreeningDto() =
    CreateScreeningDto(
        movieId = movieId.value,
        roomId = room.id.value,
        startTime = startTime.value,
    )

private fun Screening.toListItemDto() =
    ScreeningListItemDto(
        id = id.value,
        startTime = startTime.value,
        room = room.toScreeningListItemRoom(),
    )

private fun Room.toScreeningListItemRoom() =
    ScreeningListItemRoomDto(
        id = id.value,
        number = number.value,
    )

private fun HttpRequestBuilder.withQueryParams(vararg params: Pair<String, Any>) {
    for ((key, value) in params) {
        parameter(key, value)
    }
}
