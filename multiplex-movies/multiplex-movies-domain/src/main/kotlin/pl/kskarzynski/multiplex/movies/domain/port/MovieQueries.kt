package pl.kskarzynski.multiplex.movies.domain.port

import pl.kskarzynski.multiplex.movies.api.model.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId

interface MovieQueries {
    suspend fun findMovie(movieId: MovieId): Movie?
}
