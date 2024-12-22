@file:UseSerializers(UuidSerializer::class, NonEmptyListSerializer::class)

package pl.kskarzynski.multiplex.rooms.service.rest.dto

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer

@Serializable
data class RoomDto(
    val id: UUID,
    val number: Int,
    val seats: NonEmptyList<SeatDto>,
)
