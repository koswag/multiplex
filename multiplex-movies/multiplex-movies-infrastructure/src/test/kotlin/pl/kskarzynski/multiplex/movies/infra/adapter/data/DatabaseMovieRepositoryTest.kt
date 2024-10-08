package pl.kskarzynski.multiplex.movies.infra.adapter.data

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FeatureSpec
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import pl.kskarzynski.multiplex.common.test.exposed.initializeDatabase
import pl.kskarzynski.multiplex.common.test.testcontainers.installPostgresContainer
import pl.kskarzynski.multiplex.movies.api.model.Movie
import pl.kskarzynski.multiplex.movies.api.model.MovieTitle
import pl.kskarzynski.multiplex.movies.infra.adapter.data.table.MovieTable
import pl.kskarzynski.multiplex.movies.infra.util.movie
import pl.kskarzynski.multiplex.shared.movie.MovieId
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class DatabaseMovieRepositoryTest : FeatureSpec({

    isolationMode = IsolationMode.InstancePerTest

    beforeSpec {
        val dataSource = installPostgresContainer()
        initializeDatabase(dataSource, MovieTable)
    }

    feature("Saving a movie") {
        scenario("Table is empty") {
            // given:
            val movie = movie()

            // when:
            transaction {
                MovieTable.upsert(movie)
            }

            // then:
            val foundMovie = findMovie(movie.id)

            expectThat(foundMovie)
                .isNotNull()
                .isEqualTo(movie)
        }

        scenario("Other movie exists") {
            // given:
            val movie = movie()

            val otherMovie = movie()
            insertMovie(otherMovie)

            // when:
            transaction {
                MovieTable.upsert(movie)
            }

            // then:
            val foundMovie = findMovie(movie.id)
            val foundOtherMovie = findMovie(otherMovie.id)

            expect {
                that(foundMovie)
                    .isNotNull()
                    .isEqualTo(movie)

                that(foundOtherMovie)
                    .isNotNull()
                    .isEqualTo(otherMovie)
            }
        }

        scenario("Updating existent movie") {
            // given:
            val movie = movie()
            insertMovie(movie)

            // when:
            val updatedMovie = movie.copy(title = MovieTitle("New Title"))
            transaction {
                MovieTable.upsert(updatedMovie)
            }

            // then:
            val foundMovie = findMovie(movie.id)

            expectThat(foundMovie)
                .isNotNull()
                .isEqualTo(updatedMovie)
        }
    }

    feature("Finding a movie") {
        scenario("Table is empty") {
            // given:
            val nonExistentMovieId = MovieId.generate()

            // when:
            val foundMovie =
                transaction {
                    MovieTable.find(nonExistentMovieId)
                }

            // then:
            expectThat(foundMovie).isNull()
        }

        scenario("Other movie exists") {
            // given
            val nonExistentMovieId = MovieId.generate()

            val existentMovie = movie()
            insertMovie(existentMovie)

            // when:
            val foundMovie =
                transaction {
                    MovieTable.find(nonExistentMovieId)
                }

            // then:
            expectThat(foundMovie).isNull()
        }

        scenario("The movie exists") {
            // given:
            val movie = movie()
            insertMovie(movie)

            // when:
            val foundMovie =
                transaction {
                    MovieTable.find(movie.id)
                }

            // then:
            expectThat(foundMovie)
                .isNotNull()
                .isEqualTo(movie)
        }
    }

})

private fun findMovie(movieId: MovieId): Movie? =
    transaction {
        with(MovieTable) {
            select(id, title, year)
                .where { id eq movieId.value }
                .firstOrNull()
                ?.let { rowToDomain(it) }
        }
    }

private fun insertMovie(movie: Movie) {
    transaction {
        with(MovieTable) {
            insert {
                it[id] = movie.id.value
                it[title] = movie.title.value
                it[year] = movie.releaseYear.value
            }
        }
    }
}
