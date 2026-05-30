@file:UseSerializers(NonEmptyListSerializer::class)
@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.rooms.service.rest.dto

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.shared.room.Room
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class RoomDto(
    val id: Uuid,
    val number: Int,
    val seats: NonEmptyList<SeatDto>,
)

fun Room.toDto() =
    RoomDto(
        id = id.value,
        number = number.value,
        seats = seats.map { it.toDto() },
    )
