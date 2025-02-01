@file:UseSerializers(NonEmptyListSerializer::class)

package pl.kskarzynski.multiplex.rooms.service.rest.dto

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber

@Serializable
data class CreateRoomDto(
    val number: Int,
    val seats: NonEmptyList<SeatDto>,
)

fun CreateRoomDto.toDomain() =
    Room(
        id = RoomId.generate(),
        number = RoomNumber(number),
        seats = seats.map { it.toDomain() },
    )
