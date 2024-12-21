package pl.kskarzynski.multiplex.integration.movies

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.koin.KoinExtension
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.checkAll
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import org.koin.test.KoinTest
import org.koin.test.inject
import pl.kskarzynski.multiplex.common.infra.ktor.CONTENT_TYPE_JSON_UTF_8
import pl.kskarzynski.multiplex.common.test.arbs.movie
import pl.kskarzynski.multiplex.common.test.arbs.movieId
import pl.kskarzynski.multiplex.common.test.arbs.movieReleaseYear
import pl.kskarzynski.multiplex.common.test.arbs.movieTitle
import pl.kskarzynski.multiplex.common.test.exposed.initializeDatabase
import pl.kskarzynski.multiplex.common.test.testcontainers.installPostgresContainer
import pl.kskarzynski.multiplex.integration.clientWithJson
import pl.kskarzynski.multiplex.integration.multiplexApplication
import pl.kskarzynski.multiplex.movies.service.config.MovieModule
import pl.kskarzynski.multiplex.movies.service.data.MovieRepository
import pl.kskarzynski.multiplex.movies.service.data.table.MovieTable
import pl.kskarzynski.multiplex.movies.service.rest.dto.CreateMovieDto
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieDto
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieValidationError
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieValidationError.InvalidMovieReleaseYear
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieValidationError.MovieTitleIsEmpty
import pl.kskarzynski.multiplex.movies.service.rest.dto.PatchMovieDto
import pl.kskarzynski.multiplex.shared.movie.MovieReleaseYear
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.one

class MoviesIntegrationTest : KoinTest, FeatureSpec() {

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
                    multiplexApplication()

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
                    multiplexApplication()
                    val client = createClient {
                        install(ContentNegotiation) { json() }
                    }

                    val existentMovie = Arb.movie().next()
                    movieRepository.save(existentMovie)

                    // when:
                    val response = client.get("/api/movies/${existentMovie.id}")

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
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
                    multiplexApplication()
                    val client = clientWithJson()

                    // when:
                    val createMovieDto = CreateMovieDto(
                        title = Arb.movieTitle().next().value,
                        releaseYear = Arb.movieReleaseYear().next().value,
                    )
                    val response = client.post("/api/movies") {
                        contentType(ContentType.Application.Json)
                        setBody(createMovieDto)
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.Created
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
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
                    multiplexApplication()
                    val client = clientWithJson()

                    checkAll(
                        Arb.int(1..<MovieReleaseYear.MIN_VALUE),
                    ) { invalidReleaseYear ->
                        val invalidTitle = ""

                        // when:
                        val createMovieDto = CreateMovieDto(
                            title = invalidTitle,
                            releaseYear = invalidReleaseYear,
                        )
                        val response = client.post("/api/movies") {
                            contentType(ContentType.Application.Json)
                            setBody(createMovieDto)
                        }

                        // then:
                        expectThat(response) {
                            get { status } isEqualTo HttpStatusCode.BadRequest
                            get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
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
                                    get { minValue } isEqualTo MovieReleaseYear.MIN_VALUE
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
                    multiplexApplication()
                    val client = clientWithJson()

                    // when:
                    val nonExistentMovieId = Arb.movieId().next()
                    val patchMovieDto = PatchMovieDto()
                    val response = client.patch("/api/movies/$nonExistentMovieId") {
                        contentType(ContentType.Application.Json)
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
                    multiplexApplication()
                    val client = clientWithJson()
                    val existentMovie = Arb.movie().next()
                        .also { movieRepository.save(it) }

                    // when:
                    val patchMovieDto = PatchMovieDto(
                        title = Arb.movieTitle().next().value,
                        releaseYear = Arb.movieReleaseYear().next().value,
                    )
                    val response = client.patch("/api/movies/${existentMovie.id}") {
                        contentType(ContentType.Application.Json)
                        setBody(patchMovieDto)
                    }

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
                    }

                    expectThat(response.body<MovieDto>()) {
                        get { title } isEqualTo patchMovieDto.title
                        get { releaseYear } isEqualTo patchMovieDto.releaseYear
                    }

                    val updatedMovie = client.get("api/movies/${existentMovie.id}").body<MovieDto>()
                    expectThat(updatedMovie) {
                        get { title } isEqualTo patchMovieDto.title
                        get { releaseYear } isEqualTo patchMovieDto.releaseYear
                    }
                }
            }

            scenario("Movie patch is invalid") {
                testApplication {
                    // given:
                    multiplexApplication()
                    val client = clientWithJson()
                    val existentMovie = Arb.movie().next()
                        .also { movieRepository.save(it) }

                    checkAll(
                        Arb.int(1..<MovieReleaseYear.MIN_VALUE),
                    ) { invalidReleaseYear ->
                        val invalidTitle = ""

                        // when:
                        val patchMovieDto = CreateMovieDto(
                            title = invalidTitle,
                            releaseYear = invalidReleaseYear,
                        )
                        val response = client.patch("/api/movies/${existentMovie.id}") {
                            contentType(ContentType.Application.Json)
                            setBody(patchMovieDto)
                        }

                        // then:
                        expectThat(response) {
                            get { status } isEqualTo HttpStatusCode.BadRequest
                            get { contentType() } isEqualTo CONTENT_TYPE_JSON_UTF_8
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
                                    get { minValue } isEqualTo MovieReleaseYear.MIN_VALUE
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
