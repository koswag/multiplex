@file:UseSerializers(UuidSerializer::class)
@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.shared.movie.Movie
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ScreeningMovieDto(
    val id: Uuid,
    val title: String,
    val releaseYear: Int,
)

fun Movie.toDto() =
    ScreeningMovieDto(
        id = id.value,
        title = title.value,
        releaseYear = releaseYear.value,
    )
