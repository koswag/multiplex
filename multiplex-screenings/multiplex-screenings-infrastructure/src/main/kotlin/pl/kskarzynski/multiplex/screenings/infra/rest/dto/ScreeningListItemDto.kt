@file:UseSerializers(LocalDateTimeSerializer::class)
@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.LocalDateTimeSerializer
import pl.kskarzynski.multiplex.screenings.domain.model.view.ScreeningListItemView
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ScreeningListItemDto(
    val id: Uuid,
    val startTime: LocalDateTime,
    val room: ScreeningListItemRoomDto,
)

fun ScreeningListItemView.toDto() =
    ScreeningListItemDto(
        id = id.value,
        startTime = startTime.value,
        room = ScreeningListItemRoomDto(
            id = room.id.value,
            number = room.number.value,
        ),
    )
