package pl.kskarzynski.multiplex.movies.infra.rest.dto

import java.util.UUID

data class MovieDto(
    val id: UUID,
    val title: String,
    val releaseYear: Int,
)
