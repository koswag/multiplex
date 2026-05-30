@file:UseSerializers(UuidSerializer::class, LocalDateTimeSerializer::class)
@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.LocalDateTimeSerializer
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
sealed interface ScreeningValidationErrorDto {

    @Serializable
    data class MovieDoesNotExist(val movieId: Uuid) : ScreeningValidationErrorDto

    @Serializable
    data class RoomDoesNotExist(val roomId: Uuid) : ScreeningValidationErrorDto

    @Serializable
    data class PastScreeningTime(val screeningTime: LocalDateTime) : ScreeningValidationErrorDto
}