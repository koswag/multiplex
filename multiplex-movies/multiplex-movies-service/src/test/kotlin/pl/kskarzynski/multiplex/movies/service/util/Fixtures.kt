@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.movies.service.util

import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.movie.MovieReleaseYear
import pl.kskarzynski.multiplex.shared.movie.MovieTitle
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val DEFAULT_MOVIE_TITLE = MovieTitle("Kingdom of Heaven")
val DEFAULT_RELEASE_YEAR = MovieReleaseYear(2005)

fun movie(
    id: Uuid = Uuid.random(),
    title: String = DEFAULT_MOVIE_TITLE.value,
    releaseYear: Int = DEFAULT_RELEASE_YEAR.value,
) =
    Movie(
        id = MovieId(id),
        title = MovieTitle(title),
        releaseYear = MovieReleaseYear(releaseYear),
    )
