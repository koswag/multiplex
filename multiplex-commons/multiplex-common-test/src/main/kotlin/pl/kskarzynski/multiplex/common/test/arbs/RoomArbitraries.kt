package pl.kskarzynski.multiplex.common.test.arbs

import arrow.core.NonEmptyList
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.positiveInt
import io.kotest.property.arbitrary.uuid
import pl.kskarzynski.multiplex.common.utils.arrow.toNonEmptyList
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

private const val DEFAULT_MAX_ROOM_NUMBER = 20
private const val DEFAULT_MAX_ROW_COUNT = 10
private const val DEFAULT_MAX_ROW_SIZE = 15

fun Arb.Companion.room(
    maxRoomNumber: Int = DEFAULT_MAX_ROOM_NUMBER,
    maxRowCount: Int = DEFAULT_MAX_ROW_COUNT,
    maxRowSize: Int = DEFAULT_MAX_ROW_SIZE,
): Arb<Room> =
    arbitrary {
        val id = Arb.roomId().bind()
        val number = Arb.roomNumber(maxRoomNumber).bind()
        val seats = Arb.seats(maxRowCount, maxRowSize).bind()
        Room(id, number, seats)
    }

fun Arb.Companion.seats(
    maxRowCount: Int = DEFAULT_MAX_ROW_COUNT,
    maxRowSize: Int = DEFAULT_MAX_ROW_SIZE,
): Arb<NonEmptyList<Seat>> =
    arbitrary {
        val rowCount = Arb.int(2..maxRowCount).bind()
        val rowSize = Arb.int(2..maxRowSize).bind()
        buildList {
            for (rowNo in 1..rowCount) {
                for (seatNo in 1..rowSize) {
                    add(Seat(SeatRow(rowNo), SeatNumber(seatNo)))
                }
            }
        }.toNonEmptyList()
    }

fun Arb.Companion.roomId(): Arb<RoomId> =
    Arb.uuid().map { RoomId(it) }

fun Arb.Companion.roomNumber(max: Int = DEFAULT_MAX_ROOM_NUMBER): Arb<RoomNumber> =
    Arb.positiveInt(max).map { RoomNumber(it) }

fun Arb.Companion.seat(): Arb<Seat> =
    arbitrary {
        val row = Arb.seatRow().bind()
        val number = Arb.seatNumber().bind()
        Seat(row, number)
    }

fun Arb.Companion.seatRow(): Arb<SeatRow> =
    Arb.positiveInt(max = DEFAULT_MAX_ROW_COUNT).map { SeatRow(it) }

fun Arb.Companion.seatNumber(): Arb<SeatNumber> =
    Arb.positiveInt(max = DEFAULT_MAX_ROW_SIZE).map { SeatNumber(it) }
