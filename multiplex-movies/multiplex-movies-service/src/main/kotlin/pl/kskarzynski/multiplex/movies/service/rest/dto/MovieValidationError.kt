package pl.kskarzynski.multiplex.movies.service.rest.dto

import kotlinx.serialization.Serializable
import pl.kskarzynski.multiplex.shared.movie.MovieReleaseYear

@Serializable
sealed interface MovieValidationError {

    val type: String
        get() = this::class.simpleName!!

    @Serializable
    data object MovieTitleIsEmpty : MovieValidationError

    @Serializable
    data class InvalidMovieReleaseYear(val releaseYear: Int) : MovieValidationError {
        val minValue = MovieReleaseYear.MIN_VALUE
    }
}
