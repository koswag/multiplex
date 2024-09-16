package pl.kskarzynski.multiplex.domain.model.screening

import arrow.core.EitherNel
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
            val seatsToMark = findSeats(seatPlacements).bind()
            val markedSeats = markSeatsAsTaken(seatsToMark).bind()

            val updatedSeats = updateSeats(markedSeats)
            ensureNoSingleSeats(updatedSeats).bind()

            copy(seats = updatedSeats)
        }

    private fun findSeats(seatPlacements: List<SeatPlacement>): EitherNel<SeatDoesNotExist, List<Seat>> =
        either {
            mapOrAccumulate(seatPlacements) { seatPlacement ->
                val seat = seats.find { it.placement == seatPlacement }
                ensureNotNull(seat) { SeatDoesNotExist(seatPlacement) }
            }
        }

    private fun markSeatsAsTaken(seats: List<Seat>): EitherNel<SeatAlreadyTaken, List<Seat>> =
        either {
            mapOrAccumulate(seats) { seat ->
                seat.markAsTaken().bind()
            }
        }

    private fun updateSeats(modifiedSeats: List<Seat>): List<Seat> {
        val modifiedSeatPlacements = modifiedSeats.map { it.placement }.toSet()
        return seats.filter { it.placement !in modifiedSeatPlacements } + modifiedSeats
    }

    private fun ensureNoSingleSeats(seats: List<Seat>): EitherNel<SeatIsSingle, Unit> =
        either {
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
