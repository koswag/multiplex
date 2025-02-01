@file:UseSerializers(UuidSerializer::class, NonEmptyListSerializer::class)

package pl.kskarzynski.multiplex.rooms.service.rest.dto

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.shared.room.Room

@Serializable
data class RoomDto(
    val id: UUID,
    val number: Int,
    val seats: NonEmptyList<SeatDto>,
)

fun Room.toDto() =
    RoomDto(
        id = id.value,
        number = number.value,
        seats = seats.map { it.toDto() },
    )
