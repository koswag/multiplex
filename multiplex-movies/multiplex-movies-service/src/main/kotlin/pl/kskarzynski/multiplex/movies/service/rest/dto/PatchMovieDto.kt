package pl.kskarzynski.multiplex.movies.service.rest.dto

import kotlinx.serialization.Serializable

@Serializable
data class PatchMovieDto(
    val title: String? = null,
    val releaseYear: Int? = null,
)
