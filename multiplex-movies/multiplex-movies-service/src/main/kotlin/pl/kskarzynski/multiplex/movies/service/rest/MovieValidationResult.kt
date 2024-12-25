package pl.kskarzynski.multiplex.movies.service.rest

import arrow.core.NonEmptyList
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieDto
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieValidationError

sealed interface MovieValidationResult {
    data class Success(val movie: MovieDto) : MovieValidationResult
    data class Failure(val errors: NonEmptyList<MovieValidationError>) : MovieValidationResult
}
