@file:UseSerializers(NonEmptyListSerializer::class)

package pl.kskarzynski.multiplex.rooms.service.rest.dto

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomNumber

@Serializable
data class PatchRoomDto(
    val number: Int? = null,
    val seats: NonEmptyList<SeatDto>? = null,
)

fun Room.applyPatch(patch: PatchRoomDto): Room =
    copy(
        number = patch.number?.let { RoomNumber(it) } ?: number,
        seats = patch.seats?.map { it.toDomain() } ?: seats,
    )
