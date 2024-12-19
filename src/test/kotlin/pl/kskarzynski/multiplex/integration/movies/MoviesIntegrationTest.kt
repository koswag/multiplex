package pl.kskarzynski.multiplex.integration.movies

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.koin.KoinExtension
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import org.koin.test.KoinTest
import org.koin.test.inject
import pl.kskarzynski.multiplex.common.test.arbs.movie
import pl.kskarzynski.multiplex.common.test.arbs.movieId
import pl.kskarzynski.multiplex.common.test.exposed.initializeDatabase
import pl.kskarzynski.multiplex.common.test.testcontainers.installPostgresContainer
import pl.kskarzynski.multiplex.integration.multiplexApplication
import pl.kskarzynski.multiplex.movies.service.adapter.data.MovieRepository
import pl.kskarzynski.multiplex.movies.service.adapter.data.table.MovieTable
import pl.kskarzynski.multiplex.movies.service.config.MovieModule
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieDto
import strikt.api.expectThat
import strikt.assertions.isEqualTo

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

                    val movie = Arb.movie().next()
                    movieRepository.save(movie)

                    // when:
                    val response = client.get("/api/movies/${movie.id}")

                    // then:
                    expectThat(response) {
                        get { status } isEqualTo HttpStatusCode.OK
                        get { contentType() } isEqualTo ContentType.Application.Json.withCharset(Charsets.UTF_8)
                    }

                    expectThat(response.body<MovieDto>()) {
                        get { id } isEqualTo movie.id.value
                        get { title } isEqualTo movie.title.value
                        get { releaseYear } isEqualTo movie.releaseYear.value
                    }
                }
            }
        }
    }
}
