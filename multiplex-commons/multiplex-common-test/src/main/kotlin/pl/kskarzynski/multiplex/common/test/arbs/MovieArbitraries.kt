@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.common.test.arbs

import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.movie.MovieReleaseYear
import pl.kskarzynski.multiplex.shared.movie.MovieTitle
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

fun Arb.Companion.movieId(): Arb<MovieId> =
    uuid().map { MovieId(it.toKotlinUuid()) }

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
