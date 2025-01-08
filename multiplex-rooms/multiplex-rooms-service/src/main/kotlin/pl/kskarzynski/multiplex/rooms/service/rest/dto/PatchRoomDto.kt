@file:UseSerializers(NonEmptyListSerializer::class)

package pl.kskarzynski.multiplex.rooms.service.rest.dto

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class PatchRoomDto(
    val number: Int? = null,
    val seats: NonEmptyList<SeatDto>? = null,
)
