@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.movies.service.rest.dto

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class MovieDto(
    val id: Uuid,
    val title: String,
    val releaseYear: Int,
)
