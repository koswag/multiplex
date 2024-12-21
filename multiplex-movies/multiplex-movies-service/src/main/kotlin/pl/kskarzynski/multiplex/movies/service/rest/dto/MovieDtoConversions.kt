package pl.kskarzynski.multiplex.movies.service.rest.dto

import arrow.core.EitherNel
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import pl.kskarzynski.multiplex.common.utils.arrow.accumulateErrors
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieValidationError.InvalidMovieReleaseYear
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieValidationError.MovieTitleIsEmpty
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.movie.MovieReleaseYear
import pl.kskarzynski.multiplex.shared.movie.MovieTitle

fun Movie.toDto() =
    MovieDto(
        id = id.value,
        title = title.value,
        releaseYear = releaseYear.value,
    )

fun Movie.applyPatch(patch: PatchMovieDto): EitherNel<MovieValidationError, Movie> =
    either {
        accumulateErrors(
            { ensureValidTitle(patch.title) },
            { ensureValidReleaseYear(patch.releaseYear) },
        )

        copy(
            title = patch.title?.let(::MovieTitle) ?: title,
            releaseYear = patch.releaseYear?.let(::MovieReleaseYear) ?: releaseYear,
        )
    }

fun CreateMovieDto.toDomain(): EitherNel<MovieValidationError, Movie> =
    either {
        accumulateErrors(
            { ensureValidTitle(title) },
            { ensureValidReleaseYear(releaseYear) },
        )

        Movie(
            id = MovieId.generate(),
            title = MovieTitle(title),
            releaseYear = MovieReleaseYear(releaseYear),
        )
    }

private fun Raise<MovieTitleIsEmpty>.ensureValidTitle(title: String?) {
    if (title != null) {
        ensure(title.isNotEmpty()) { MovieTitleIsEmpty }
    }
}

private fun Raise<InvalidMovieReleaseYear>.ensureValidReleaseYear(releaseYear: Int?) {
    if (releaseYear != null) {
        ensure(releaseYear >= MovieReleaseYear.MIN_VALUE) { InvalidMovieReleaseYear(releaseYear) }
    }
}
