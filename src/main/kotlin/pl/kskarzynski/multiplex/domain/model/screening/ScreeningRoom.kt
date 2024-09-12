package pl.kskarzynski.multiplex.domain.model.screening

import arrow.core.EitherNel
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.raise.mapOrAccumulate
import pl.kskarzynski.multiplex.domain.model.screening.BookingError.SeatAlreadyTaken
import pl.kskarzynski.multiplex.domain.model.screening.BookingError.SeatDoesNotExist
import pl.kskarzynski.multiplex.domain.model.screening.BookingError.SeatIsSingle

data class ScreeningRoom(
    val number: RoomNumber,
    val seats: List<Seat>,
) {
    fun bookSeats(seatPlacements: List<SeatPlacement>): EitherNel<BookingError, ScreeningRoom> =
        either {
            val seatsToMark = findSeats(seatPlacements)
            val markedSeats = markSeatsAsTaken(seatsToMark)

            val updatedSeats = updateSeats(markedSeats)
            ensureNoSingleSeats(updatedSeats)

            copy(seats = updatedSeats)
        }

    context(Raise<NonEmptyList<SeatDoesNotExist>>)
    private fun findSeats(seatPlacements: List<SeatPlacement>): List<Seat> =
        mapOrAccumulate(seatPlacements) { seatPlacement ->
            val seat = seats.find { it.placement == seatPlacement }
            ensureNotNull(seat) { SeatDoesNotExist(seatPlacement) }
        }

    context(Raise<NonEmptyList<SeatAlreadyTaken>>)
    private fun markSeatsAsTaken(seats: List<Seat>): List<Seat> =
        mapOrAccumulate(seats) { seat ->
            seat.markAsTaken().bind()
        }

    private fun updateSeats(modifiedSeats: List<Seat>): List<Seat> {
        val modifiedSeatPlacements = modifiedSeats.map { it.placement }.toSet()
        return seats.filter { it.placement !in modifiedSeatPlacements } + modifiedSeats
    }

    context(Raise<NonEmptyList<SeatIsSingle>>)
    private fun ensureNoSingleSeats(seats: List<Seat>) {
        val singleSeats = findSingleSeats(seats)
        mapOrAccumulate(singleSeats) { seatPlacement ->
            raise(SeatIsSingle(seatPlacement))
        }
    }
}

private fun findSingleSeats(allSeats: List<Seat>): Collection<SeatPlacement> =
    allSeats.groupBy { it.placement.row }
        .values
        .flatMap { row -> findSingleSeatsInRow(row) }

private fun findSingleSeatsInRow(row: List<Seat>): List<SeatPlacement> {
    val sortedSeats = row.sortedBy { it.placement.number.value }

    fun isSingleSeat(seatIndex: Int, seat: Seat): Boolean {
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
