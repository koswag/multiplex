package pl.kskarzynski.multiplex.movies.infra.util

import java.util.UUID
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.movie.MovieReleaseYear
import pl.kskarzynski.multiplex.shared.movie.MovieTitle

val DEFAULT_MOVIE_TITLE = MovieTitle("Kingdom of Heaven")
val DEFAULT_RELEASE_YEAR = MovieReleaseYear(2005)

fun movie(
    id: UUID = UUID.randomUUID(),
    title: String = DEFAULT_MOVIE_TITLE.value,
    releaseYear: Int = DEFAULT_RELEASE_YEAR.value,
) =
    Movie(
        id = MovieId(id),
        title = MovieTitle(title),
        releaseYear = MovieReleaseYear(releaseYear),
    )
