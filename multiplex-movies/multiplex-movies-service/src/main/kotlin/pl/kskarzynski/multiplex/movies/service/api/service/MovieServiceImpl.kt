package pl.kskarzynski.multiplex.movies.service.api.service

import pl.kskarzynski.multiplex.movies.api.service.MovieService
import pl.kskarzynski.multiplex.movies.service.data.MovieQueries
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId

class MovieServiceImpl(
    private val movieQueries: MovieQueries,
) : MovieService {

    override suspend fun findMovie(movieId: MovieId): Movie? =
        movieQueries.findById(movieId)
}
