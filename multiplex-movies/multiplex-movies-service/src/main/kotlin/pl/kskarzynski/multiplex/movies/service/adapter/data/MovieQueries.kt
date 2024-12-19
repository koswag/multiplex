package pl.kskarzynski.multiplex.movies.service.adapter.data

import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId

interface MovieQueries {
    suspend fun findById(movieId: MovieId): Movie?
}
