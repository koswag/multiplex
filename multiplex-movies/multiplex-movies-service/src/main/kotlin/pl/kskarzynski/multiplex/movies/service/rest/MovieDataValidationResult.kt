package pl.kskarzynski.multiplex.movies.service.rest

import arrow.core.NonEmptyList
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieDto
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieValidationError

sealed interface MovieDataValidationResult {
    data class Success(val movie: MovieDto) : MovieDataValidationResult
    data class Failure(val errors: NonEmptyList<MovieValidationError>) : MovieDataValidationResult
}
