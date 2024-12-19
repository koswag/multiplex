@file:UseSerializers(UuidSerializer::class)

package pl.kskarzynski.multiplex.movies.service.rest.dto

import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer

@Serializable
data class MovieDto(
    val id: UUID,
    val title: String,
    val releaseYear: Int,
)
