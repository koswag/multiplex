package pl.kskarzynski.multiplex.common.test.arbs

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.uuid
import pl.kskarzynski.multiplex.common.utils.arrow.toNonEmptyList
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

private const val DEFAULT_MAX_ROOM_NUMBER = 20
private const val DEFAULT_ROW_COUNT = 10
private const val DEFAULT_ROW_SIZE = 15

fun Arb.Companion.room(
    maxRoomNumber: Int = DEFAULT_MAX_ROOM_NUMBER,
    rowCount: Int = DEFAULT_ROW_COUNT,
    rowSize: Int = DEFAULT_ROW_SIZE,
): Arb<Room> =
    arbitrary {
        val id = roomId().bind()
        val number = roomNumber(maxRoomNumber).bind()

        val seats = buildList {
            for (rowNo in 1..rowCount) {
                for (seatNo in 1..rowSize) {
                    add(Seat(SeatRow(rowNo), SeatNumber(seatNo)))
                }
            }
        }.toNonEmptyList()

        Room(id, number, seats)
    }

fun Arb.Companion.roomId(): Arb<RoomId> =
    Arb.uuid().map { RoomId(it) }

fun Arb.Companion.roomNumber(max: Int = DEFAULT_MAX_ROOM_NUMBER): Arb<RoomNumber> =
    Arb.int(1..max).map { RoomNumber(it) }
