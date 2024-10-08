package pl.kskarzynski.multiplex.movies.infra.adapter.api.service

import pl.kskarzynski.multiplex.movies.api.service.MovieService
import pl.kskarzynski.multiplex.movies.domain.port.MovieQueries
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId

class MovieServiceImpl(
    private val movieQueries: MovieQueries,
) : MovieService {

    override suspend fun findMovie(movieId: MovieId): Movie? =
        movieQueries.findMovie(movieId)
}
