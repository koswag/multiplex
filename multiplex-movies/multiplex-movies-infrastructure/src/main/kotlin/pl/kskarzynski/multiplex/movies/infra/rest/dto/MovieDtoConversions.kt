package pl.kskarzynski.multiplex.movies.infra.rest.dto

import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.movie.MovieReleaseYear
import pl.kskarzynski.multiplex.shared.movie.MovieTitle

fun Movie.toDto() =
    MovieDto(
        id = id.value,
        title = title.value,
        releaseYear = releaseYear.value,
    )

fun MovieDto.toDomain() =
    Movie(
        id = MovieId(id),
        title = MovieTitle(title),
        releaseYear = MovieReleaseYear(releaseYear),
    )
