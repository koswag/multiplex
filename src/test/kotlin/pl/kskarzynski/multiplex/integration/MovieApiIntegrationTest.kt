@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.integration

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.koin.KoinExtension
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.checkAll
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.server.testing.*
import org.koin.test.KoinTest
import org.koin.test.inject
import pl.kskarzynski.multiplex.common.test.arbs.movie
import pl.kskarzynski.multiplex.common.test.arbs.movieId
import pl.kskarzynski.multiplex.common.test.arbs.movieReleaseYear
import pl.kskarzynski.multiplex.common.test.arbs.movieTitle
import pl.kskarzynski.multiplex.common.test.exposed.initializeDatabase
import pl.kskarzynski.multiplex.common.test.testcontainers.installPostgresContainer
import pl.kskarzynski.multiplex.installPlugins
import pl.kskarzynski.multiplex.integration.util.assertions.hasContentTypeJsonUtf8
import pl.kskarzynski.multiplex.movies.service.config.MovieModule
import pl.kskarzynski.multiplex.movies.service.data.MovieRepository
import pl.kskarzynski.multiplex.movies.service.data.table.MovieTable
import pl.kskarzynski.multiplex.movies.service.rest.dto.CreateMovieDto
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieDto
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieValidationError
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieValidationError.InvalidMovieReleaseYear
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieValidationError.MovieTitleIsEmpty
import pl.kskarzynski.multiplex.movies.service.rest.dto.PatchMovieDto
import pl.kskarzynski.multiplex.movies.service.rest.movieModule
import pl.kskarzynski.multiplex.shared.movie.MovieReleaseYear.Companion.MIN_VALUE
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.one
import kotlin.uuid.ExperimentalUuidApi

class MovieApiIntegrationTest : KoinTest, FeatureSpec() {

    private val movieRepository by inject<MovieRepository>()

    init {
        isolationMode = IsolationMode.InstancePerLeaf
        extensions(KoinExtension(MovieModule))

        beforeSpec {
            val datasource = installPostgresContainer()
            initializeDatabase(datasource, MovieTable)
        }

        feature("Getting a Movie") {
            scenario("Movie does not exist") {
                testApplication {
                    // given:
                    setupMultiplexApplication()

                    // when:
                    val nonExistentId = Arb.movieId().next()
                    val response = client.get("/api/movies/$nonExistentId")

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.NotFound
                    }
                }
            }

            scenario("Movie exists") {
                testApplication {
                    // given:
                    setupMultiplexApplication()
                    val client = configureClient()

                    val existentMovie = Arb.movie().next()
                        .also { movieRepository.save(it) }

                    // when:
                    val response = client.get("/api/movies/${existentMovie.id}")

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        hasContentTypeJsonUtf8()
                    }

                    expectThat(response.body<MovieDto>()) {
                        get { id } isEqualTo existentMovie.id.value
                        get { title } isEqualTo existentMovie.title.value
                        get { releaseYear } isEqualTo existentMovie.releaseYear.value
                    }
                }
            }
        }

        feature("Creating a Movie") {
            scenario("Movie is valid") {
                testApplication {
                    // given:
                    setupMultiplexApplication()
                    val client = configureClient()

                    // when:
                    val createMovieDto = CreateMovieDto(
                        title = Arb.movieTitle().next().value,
                        releaseYear = Arb.movieReleaseYear().next().value,
                    )
                    val response = client.post("/api/movies") {
                        contentType(Json)
                        setBody(createMovieDto)
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.Created
                        hasContentTypeJsonUtf8()
                    }

                    expectThat(response.body<MovieDto>()) {
                        get { title } isEqualTo createMovieDto.title
                        get { releaseYear } isEqualTo createMovieDto.releaseYear
                    }
                }
            }

            scenario("Movie is invalid") {
                testApplication {
                    // given:
                    setupMultiplexApplication()
                    val client = configureClient()

                    checkAll(
                        Arb.int(1..<MIN_VALUE),
                    ) { invalidReleaseYear ->
                        val invalidTitle = ""

                        // when:
                        val createMovieDto = CreateMovieDto(
                            title = invalidTitle,
                            releaseYear = invalidReleaseYear,
                        )
                        val response = client.post("/api/movies") {
                            contentType(Json)
                            setBody(createMovieDto)
                        }

                        // then:
                        expectThat(response) {
                            get { status } isEqualTo HttpStatusCode.BadRequest
                            hasContentTypeJsonUtf8()
                        }

                        expectThat(response.body<List<MovieValidationError>>()) {
                            hasSize(2)
                            one {
                                isA<MovieTitleIsEmpty>() and {
                                    get { type } isEqualTo MovieTitleIsEmpty::class.simpleName
                                }
                            }
                            one {
                                isA<InvalidMovieReleaseYear>() and {
                                    get { type } isEqualTo InvalidMovieReleaseYear::class.simpleName
                                    get { releaseYear } isEqualTo invalidReleaseYear
                                    get { minValue } isEqualTo MIN_VALUE
                                }
                            }
                        }
                    }
                }
            }
        }

        feature("Patching a Movie") {
            scenario("Movie does not exist") {
                testApplication {
                    // given:
                    setupMultiplexApplication()
                    val client = configureClient()

                    // when:
                    val nonExistentMovieId = Arb.movieId().next()
                    val patchMovieDto = PatchMovieDto()
                    val response = client.patch("/api/movies/$nonExistentMovieId") {
                        contentType(Json)
                        setBody(patchMovieDto)
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.NotFound
                    }
                }
            }

            scenario("Movie patch is valid") {
                testApplication {
                    // given:
                    setupMultiplexApplication()
                    val client = configureClient()
                    val existentMovie = Arb.movie().next()
                        .also { movieRepository.save(it) }

                    // when:
                    val patchMovieDto = PatchMovieDto(
                        title = Arb.movieTitle().next().value,
                        releaseYear = Arb.movieReleaseYear().next().value,
                    )
                    val response = client.patch("/api/movies/${existentMovie.id}") {
                        contentType(Json)
                        setBody(patchMovieDto)
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        hasContentTypeJsonUtf8()
                    }

                    expectThat(response.body<MovieDto>()) {
                        get { title } isEqualTo patchMovieDto.title
                        get { releaseYear } isEqualTo patchMovieDto.releaseYear
                    }

                    val updatedMovie = client.get("api/movies/${existentMovie.id}")
                        .body<MovieDto>()
                    expectThat(updatedMovie) {
                        get { title } isEqualTo patchMovieDto.title
                        get { releaseYear } isEqualTo patchMovieDto.releaseYear
                    }
                }
            }

            scenario("Movie patch is invalid") {
                testApplication {
                    // given:
                    setupMultiplexApplication()
                    val client = configureClient()
                    val existentMovie = Arb.movie().next()
                        .also { movieRepository.save(it) }

                    checkAll(Arb.int(1..<MIN_VALUE)) { invalidReleaseYear ->
                        val invalidTitle = ""

                        // when:
                        val patchMovieDto = CreateMovieDto(
                            title = invalidTitle,
                            releaseYear = invalidReleaseYear,
                        )
                        val response = client.patch("/api/movies/${existentMovie.id}") {
                            contentType(Json)
                            setBody(patchMovieDto)
                        }

                        // then:
                        expectThat(response) {
                            get { status } isEqualTo HttpStatusCode.BadRequest
                            hasContentTypeJsonUtf8()
                        }

                        expectThat(response.body<List<MovieValidationError>>()) {
                            hasSize(2)
                            one {
                                isA<MovieTitleIsEmpty>() and {
                                    get { type } isEqualTo MovieTitleIsEmpty::class.simpleName
                                }
                            }
                            one {
                                isA<InvalidMovieReleaseYear>() and {
                                    get { type } isEqualTo InvalidMovieReleaseYear::class.simpleName
                                    get { releaseYear } isEqualTo invalidReleaseYear
                                    get { minValue } isEqualTo MIN_VALUE
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun TestApplicationBuilder.setupMultiplexApplication() {
    application {
        installPlugins()
        movieModule()
    }
}