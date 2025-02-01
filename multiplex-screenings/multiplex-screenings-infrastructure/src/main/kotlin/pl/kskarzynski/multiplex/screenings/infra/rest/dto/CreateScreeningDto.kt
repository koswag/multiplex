@file:UseSerializers(UuidSerializer::class, LocalDateTimeSerializer::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.LocalDateTimeSerializer
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer

@Serializable
data class CreateScreeningDto(
    val movieId: UUID,
    val roomId: UUID,
    val startTime: LocalDateTime,
)
