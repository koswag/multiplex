package pl.kskarzynski.multiplex.rooms.infra.util

import java.util.UUID
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

val DEFAULT_ROOM_ID = RoomId.generate()
val DEFAULT_ROOM_NUMBER = RoomNumber(1)

val DEFAULT_SEATS =
    listOf(
        seat(1, 1), seat(1, 2), seat(1, 3), seat(1, 4),
        seat(2, 1), seat(2, 2), seat(2, 3), seat(2, 4),
        seat(3, 1), seat(3, 2), seat(3, 3), seat(3, 4),
    )

fun room(
    id: UUID = RoomId.generate().value,
    number: Int = DEFAULT_ROOM_NUMBER.value,
    seats: List<Seat> = DEFAULT_SEATS,
) = Room(RoomId(id), RoomNumber(number), seats)

fun seat(row: Int, number: Int) = Seat(SeatRow(row), SeatNumber(number))
