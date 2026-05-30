package pl.kskarzynski.multiplex.movies.service.rest

import arrow.core.EitherNel
import arrow.core.NonEmptyList
import arrow.core.raise.either
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.kskarzynski.multiplex.movies.service.data.MovieRepository
import pl.kskarzynski.multiplex.movies.service.rest.MovieValidationResult.Failure
import pl.kskarzynski.multiplex.movies.service.rest.MovieValidationResult.Success
import pl.kskarzynski.multiplex.movies.service.rest.dto.*
import pl.kskarzynski.multiplex.shared.movie.MovieId

object MovieRestService : KoinComponent {

    val movieRepository by inject<MovieRepository>()

    suspend fun getMovie(id: MovieId): MovieDto? =
        movieRepository.findById(id)
            ?.toDto()

    suspend fun createMovie(dto: CreateMovieDto): MovieValidationResult =
        either {
            val movie = dto.toDomain().bind()
            movieRepository.save(movie)

            movie.toDto()
        }.toValidationResult()

    suspend fun patchMovie(id: MovieId, patch: PatchMovieDto): MovieValidationResult? {
        val movie = movieRepository.findById(id)
            ?: return null

        return either {
            val updatedMovie = movie.applyPatch(patch).bind()
            movieRepository.save(updatedMovie)

            updatedMovie.toDto()
        }.toValidationResult()
    }
}

sealed interface MovieValidationResult {
    data class Success(val movie: MovieDto) : MovieValidationResult
    data class Failure(val errors: NonEmptyList<MovieValidationError>) : MovieValidationResult
}

private fun EitherNel<MovieValidationError, MovieDto>.toValidationResult(): MovieValidationResult =
    fold(
        { errors -> Failure(errors) },
        { movie -> Success(movie) },
    )
