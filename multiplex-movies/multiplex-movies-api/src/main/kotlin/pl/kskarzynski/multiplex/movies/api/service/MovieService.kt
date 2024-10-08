package pl.kskarzynski.multiplex.movies.api.service

import pl.kskarzynski.multiplex.movies.api.model.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId

interface MovieService {
    suspend fun findMovie(movieId: MovieId): Movie?
}
