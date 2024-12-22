package pl.kskarzynski.multiplex.rooms.service.rest.dto

import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

fun Room.toDto() =
    RoomDto(
        id = id.value,
        number = number.value,
        seats = seats.map { it.toDto() },
    )

fun Seat.toDto() =
    SeatDto(
        row = row.value,
        number = number.value,
    )

fun SeatDto.toDomain() =
    Seat(
        row = SeatRow(row),
        number = SeatNumber(number),
    )

fun CreateRoomDto.toDomain() =
    Room(
        id = RoomId.generate(),
        number = RoomNumber(number),
        seats = seats.map { it.toDomain() },
    )

fun Room.applyPatch(patch: PatchRoomDto): Room =
    copy(
        number = patch.number?.let { RoomNumber(it) } ?: number,
        seats = patch.seats?.map { it.toDomain() } ?: seats,
    )
