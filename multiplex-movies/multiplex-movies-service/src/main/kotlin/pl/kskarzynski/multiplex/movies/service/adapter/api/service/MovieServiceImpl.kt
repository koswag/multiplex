package pl.kskarzynski.multiplex.movies.service.adapter.api.service

import pl.kskarzynski.multiplex.movies.api.service.MovieService
import pl.kskarzynski.multiplex.movies.service.adapter.data.MovieQueries
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId

class MovieServiceImpl(
    private val movieQueries: MovieQueries,
) : MovieService {

    override suspend fun findMovie(movieId: MovieId): Movie? =
        movieQueries.findById(movieId)
}
