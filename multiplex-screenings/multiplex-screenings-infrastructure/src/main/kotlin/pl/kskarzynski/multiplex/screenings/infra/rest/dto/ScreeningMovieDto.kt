@file:UseSerializers(UuidSerializer::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.shared.movie.Movie

@Serializable
data class ScreeningMovieDto(
    val id: UUID,
    val title: String,
    val releaseYear: Int,
)

fun Movie.toDto() =
    ScreeningMovieDto(
        id = id.value,
        title = title.value,
        releaseYear = releaseYear.value,
    )
