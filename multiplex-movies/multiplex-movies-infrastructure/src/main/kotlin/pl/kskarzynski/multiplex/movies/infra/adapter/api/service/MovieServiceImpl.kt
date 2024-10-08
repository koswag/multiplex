package pl.kskarzynski.multiplex.movies.infra.adapter.api.service

import pl.kskarzynski.multiplex.movies.api.model.Movie
import pl.kskarzynski.multiplex.movies.api.service.MovieService
import pl.kskarzynski.multiplex.movies.domain.port.MovieRepository
import pl.kskarzynski.multiplex.shared.movie.MovieId

class MovieServiceImpl(
    private val movieRepository: MovieRepository,
) : MovieService {

    override suspend fun findMovie(movieId: MovieId): Movie? =
        movieRepository.findMovie(movieId)
}
