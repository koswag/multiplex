package pl.kskarzynski.multiplex.shared.movie

data class Movie(
    val id: MovieId,
    val title: MovieTitle,
    val releaseYear: MovieReleaseYear,
)
