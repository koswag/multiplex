package pl.kskarzynski.multiplex.domain.services.validation

import arrow.core.EitherNel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import pl.kskarzynski.multiplex.domain.model.booking.Booking
import pl.kskarzynski.multiplex.domain.model.booking.BookingValidationError
import pl.kskarzynski.multiplex.domain.model.booking.BookingValidationError.*
import pl.kskarzynski.multiplex.domain.model.booking.ValidBooking
import pl.kskarzynski.multiplex.domain.model.screening.Room
import pl.kskarzynski.multiplex.domain.model.screening.Seat
import pl.kskarzynski.multiplex.domain.model.screening.SeatPlacement
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MINUTES

interface BookingValidation {

    companion object {
        const val MIN_TIME_BEFORE_SCREENING_IN_MINUTES = 15
    }

    fun validateBooking(
        currTime: LocalDateTime,
        booking: Booking,
    ): EitherNel<BookingValidationError, ValidBooking> =
        either {
            val nonExistentSeats = filterNonExistentSeats(booking)
            val alreadyTakenSeats = filterAlreadyTakenSeats(booking)
            val singleSeats = filterSingleSeats(booking)

            zipOrAccumulate(
                { ensure(notTooLateForBooking(currTime, booking)) { TooLateForBooking } },
                { ensure(booking.tickets.isNotEmpty()) { NoTickets } },
                { ensure(nonExistentSeats.isEmpty()) { NonExistentSeats(nonExistentSeats) } },
                { ensure(alreadyTakenSeats.isEmpty()) { SeatsAlreadyTaken(alreadyTakenSeats) } },
                { ensure(singleSeats.isEmpty()) { SingleSeatsLeft(singleSeats) } },
            ) { _, _, _, _, _ ->
                ValidBooking(booking)
            }
        }


    private fun notTooLateForBooking(currTime: LocalDateTime, booking: Booking): Boolean =
        MINUTES.between(currTime, booking.screening.startTime) >= MIN_TIME_BEFORE_SCREENING_IN_MINUTES


    private fun filterNonExistentSeats(booking: Booking): Collection<SeatPlacement> {
        val allSeats = booking.screening.room.seats.map { it.placement }.toSet()
        return booking.seats - allSeats
    }


    private fun filterAlreadyTakenSeats(booking: Booking): Collection<SeatPlacement> {
        val alreadyTakenSeats = booking.screening.alreadyTakenSeats.toSet()
        return booking.seats intersect alreadyTakenSeats
    }


    private fun filterSingleSeats(booking: Booking): Collection<SeatPlacement> {
        val takenSeatsAfterBooking = booking.screening.room
            .markSeatsAsTaken(booking.seats)
            .seats

        val changedRows = booking.seats.map { it.row }.toSet()

        return takenSeatsAfterBooking
            .filter { it.placement.row in changedRows }
            .groupBy { it.placement.row }
            .values
            .flatMap(::filterSingleSeats)
    }

    private fun Room.markSeatsAsTaken(seatsToMark: Collection<SeatPlacement>): Room {
        val markedSeats = seatsToMark.map { placement -> Seat(placement, isTaken = true) }

        return this.copy(
            seats = seats.filter { it.placement !in seatsToMark } + markedSeats
        )
    }

    private fun filterSingleSeats(row: Collection<Seat>): Collection<SeatPlacement> {
        val sortedSeats = row.sortedBy { it.placement.number.value }

        return sortedSeats
            .filterIndexed { i, seat ->
                val prev = sortedSeats.getOrNull(i - 1)
                val next = sortedSeats.getOrNull(i + 1)

                seat.isAvailable
                    && (prev == null || prev.isTaken)
                    && (next == null || next.isTaken)
            }
            .map { it.placement }
    }

}
