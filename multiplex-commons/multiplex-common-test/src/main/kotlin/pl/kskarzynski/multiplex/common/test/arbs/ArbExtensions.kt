package pl.kskarzynski.multiplex.common.test.arbs

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.uuid
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.movie.MovieReleaseYear
import pl.kskarzynski.multiplex.shared.movie.MovieTitle

fun Arb.Companion.movieId(): Arb<MovieId> =
    uuid().map { MovieId(it) }

fun Arb.Companion.movieTitle(): Arb<MovieTitle> =
    string(
        minSize = 3,
        maxSize = 30,
        codepoints = Codepoint.alphanumeric(),
    ).map { MovieTitle(it) }

fun Arb.Companion.movieReleaseYear(): Arb<MovieReleaseYear> =
    int(1900..2025).map { MovieReleaseYear(it) }

fun Arb.Companion.movie(): Arb<Movie> =
    arbitrary {
        val id = movieId().bind()
        val title = movieTitle().bind()
        val releaseYear = movieReleaseYear().bind()

        Movie(id, title, releaseYear)
    }
