package pl.kskarzynski.multiplex.domain.model.screening

import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.movie.MovieTitle

data class Movie(
    val id: MovieId,
    val title: MovieTitle,
)
