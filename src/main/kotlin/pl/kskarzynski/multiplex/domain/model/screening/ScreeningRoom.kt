package pl.kskarzynski.multiplex.domain.model.screening

import arrow.core.EitherNel
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.raise.mapOrAccumulate
import pl.kskarzynski.multiplex.domain.model.screening.BookingError.SeatAlreadyTaken
import pl.kskarzynski.multiplex.domain.model.screening.BookingError.SeatDoesNotExist
import pl.kskarzynski.multiplex.domain.model.screening.BookingError.SeatIsSingle
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import pl.kskarzynski.multiplex.shared.room.Seat

data class ScreeningRoom(
    val id: RoomId,
    val number: RoomNumber,
    val seats: List<ScreeningSeat>,
) {
    fun bookSeats(seatPlacements: List<Seat>): EitherNel<BookingError, ScreeningRoom> =
        either {
            val seatsToMark = findSeats(seatPlacements).bind()
            val markedSeats = markSeatsAsTaken(seatsToMark).bind()

            val updatedSeats = updateSeats(markedSeats)
            ensureNoSingleSeats(updatedSeats).bind()

            copy(seats = updatedSeats)
        }

    private fun findSeats(seatPlacements: List<Seat>): EitherNel<SeatDoesNotExist, List<ScreeningSeat>> =
        either {
            mapOrAccumulate(seatPlacements) { seatPlacement ->
                val seat = seats.find { it.placement == seatPlacement }
                ensureNotNull(seat) { SeatDoesNotExist(seatPlacement) }
            }
        }

    private fun markSeatsAsTaken(seats: List<ScreeningSeat>): EitherNel<SeatAlreadyTaken, List<ScreeningSeat>> =
        either {
            mapOrAccumulate(seats) { seat ->
                seat.markAsTaken().bind()
            }
        }

    private fun updateSeats(modifiedSeats: List<ScreeningSeat>): List<ScreeningSeat> {
        val modifiedSeatPlacements = modifiedSeats.map { it.placement }.toSet()
        return seats.filter { it.placement !in modifiedSeatPlacements } + modifiedSeats
    }

    private fun ensureNoSingleSeats(seats: List<ScreeningSeat>): EitherNel<SeatIsSingle, Unit> =
        either {
            val singleSeats = findSingleSeats(seats)
            mapOrAccumulate(singleSeats) { seatPlacement ->
                raise(SeatIsSingle(seatPlacement))
            }
        }


    fun cancelBooking(seatPlacements: List<Seat>): ScreeningRoom {
        val seatsToUnmark = findSeats(seatPlacements)
            .getOrElse { error("Seats not found: $it") }

        val unmarkedSeats = seatsToUnmark.map { it.markAsNotTaken() }
        val updatedSeats = updateSeats(unmarkedSeats)

        return copy(seats = updatedSeats)
    }
}

private fun findSingleSeats(allSeats: List<ScreeningSeat>): Collection<Seat> =
    allSeats.groupBy { it.placement.row }
        .values
        .flatMap { row -> findSingleSeatsInRow(row) }

private fun findSingleSeatsInRow(row: List<ScreeningSeat>): List<Seat> {
    val sortedSeats = row.sortedBy { it.placement.number.value }

    fun isSingleSeat(seatIndex: Int, seat: ScreeningSeat): Boolean {
        val prev = sortedSeats.getOrNull(seatIndex - 1)
        val next = sortedSeats.getOrNull(seatIndex + 1)

        return seat.isAvailable
            && (prev == null || prev.isTaken)
            && (next == null || next.isTaken)
    }

    return sortedSeats
        .filterIndexed(::isSingleSeat)
        .map { it.placement }
}
