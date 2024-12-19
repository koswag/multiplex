package pl.kskarzynski.multiplex.movies.service.rest.dto

import kotlinx.serialization.Serializable

@Serializable
data class MoviePatchDto(
    val title: String?,
    val releaseYear: Int?,
)
