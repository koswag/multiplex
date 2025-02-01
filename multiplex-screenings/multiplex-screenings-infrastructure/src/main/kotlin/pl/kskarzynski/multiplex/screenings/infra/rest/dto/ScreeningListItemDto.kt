@file:UseSerializers(UuidSerializer::class, LocalDateTimeSerializer::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.LocalDateTimeSerializer
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.screenings.domain.model.view.ScreeningListItemView

@Serializable
data class ScreeningListItemDto(
    val id: UUID,
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
