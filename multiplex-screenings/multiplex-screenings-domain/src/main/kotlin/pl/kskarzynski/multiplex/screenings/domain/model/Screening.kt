package pl.kskarzynski.multiplex.screenings.domain.model

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.right
import java.time.LocalDateTime
import pl.kskarzynski.multiplex.common.utils.arrow.accumulateErrors
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.ConfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.ExpiredBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingConfirmationError
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingConfirmationError.BookingDoesNotExist
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingConfirmationError.BookingExpired
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingError
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingError.SeatAlreadyTaken
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingError.SeatDoesNotExist
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingError.SeatIsSingle
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

data class Screening(
    val id: ScreeningId,
    val movieId: MovieId,
    val room: Room,
    val startTime: LocalDateTime,
    val bookings: List<Booking>,
) {
    private val bookingIds: Set<BookingId>
        get() = bookings.mapTo(mutableSetOf()) { it.id }

    private val allSeats: Set<Seat>
        get() = room.seats.toSet()

    private val takenSeats: Set<Seat>
        get() =
            bookings
                .filter { it is UnconfirmedBooking || it is ConfirmedBooking }
                .flatMapTo(mutableSetOf()) { it.seats }

    fun book(booking: Booking): EitherNel<BookingError, Screening> {
        if (booking.id in bookingIds) return this.right()

        return either {
            accumulateErrors(
                { ensureSeatsExist(booking) },
                { ensureSeatsNotTaken(booking) },
                { ensureNoSingleSeats(booking) },
            )

            val updatedBookings = bookings + booking
            copy(bookings = updatedBookings)
        }
    }

    context(Raise<NonEmptyList<SeatDoesNotExist>>)
    private fun ensureSeatsExist(booking: Booking) {
        val allSeats = room.seats.toSet()

        accumulateErrors(booking.seats) { seat ->
            ensure(seat in allSeats) { SeatDoesNotExist(seat) }
        }
    }

    context(Raise<NonEmptyList<SeatAlreadyTaken>>)
    private fun ensureSeatsNotTaken(booking: Booking) {
        accumulateErrors(booking.seats) { seat ->
            ensure(seat !in takenSeats) { SeatAlreadyTaken(seat) }
        }
    }

    context(Raise<NonEmptyList<SeatIsSingle>>)
    private fun ensureNoSingleSeats(booking: Booking) {
        val updatedSeats = takenSeats + booking.seats
        val singleSeats = findSingleSeats(allSeats, isTaken = { it in updatedSeats })

        accumulateErrors(singleSeats) { seat ->
            raise(SeatIsSingle(seat))
        }
    }


    fun cancelExpiredBookings(currentTime: LocalDateTime): Screening {
        val expiredBookings =
            bookings
                .filterIsInstance<UnconfirmedBooking>()
                .filter { it.expirationTime.value.isBefore(currentTime) }
                .map { it.expire() }

        val expiredBookingIds: Set<BookingId> = expiredBookings.mapTo(mutableSetOf()) { it.id }
        val nonExpiredBookings = bookings.filter { it.id !in expiredBookingIds }

        val updatedBookings = nonExpiredBookings + expiredBookings
        return copy(bookings = updatedBookings)
    }

    fun confirmBooking(
        bookingId: BookingId,
        currentTime: LocalDateTime,
    ): Either<BookingConfirmationError, Screening> =
        either {
            val booking = bookings.find { it.id == bookingId }
            ensureNotNull(booking) { BookingDoesNotExist(bookingId) }

            val confirmedBooking =
                when (booking) {
                    is ConfirmedBooking -> booking
                    is UnconfirmedBooking -> booking.confirm(currentTime).bind()
                    is ExpiredBooking -> raise(BookingExpired(booking.expirationTime))
                }

            val updatedBookings = bookings - booking + confirmedBooking
            copy(bookings = updatedBookings)
        }
}

private fun findSingleSeats(
    allSeats: Iterable<Seat>,
    isTaken: (Seat) -> Boolean,
): Collection<Seat> =
    allSeats.groupBy { it.row }
        .values
        .flatMap { row -> findSingleSeatsInRow(row, isTaken) }

private fun findSingleSeatsInRow(
    row: Iterable<Seat>,
    isTaken: (Seat) -> Boolean,
): List<Seat> {
    fun isAvailable(seat: Seat): Boolean = !isTaken(seat)

    val sortedSeats = row.sortedBy { it.number.value }

    fun isSingleSeat(seatIndex: Int, seat: Seat): Boolean {
        val prev = sortedSeats.getOrNull(seatIndex - 1)
        val next = sortedSeats.getOrNull(seatIndex + 1)

        return isAvailable(seat)
            && (prev == null || isTaken(prev))
            && (next == null || isTaken(next))
    }

    return sortedSeats
        .filterIndexed(::isSingleSeat)
}
