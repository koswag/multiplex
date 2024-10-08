package pl.kskarzynski.multiplex.movies.api.model

import pl.kskarzynski.multiplex.shared.movie.MovieId

data class Movie(
    val id: MovieId,
    val title: MovieTitle,
    val releaseYear: MovieReleaseYear,
)
