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
import pl.kskarzynski.multiplex.common.utils.collections.replace
import pl.kskarzynski.multiplex.common.utils.datetime.isBefore
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
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingError.SingleSeatLeft
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

data class Screening(
    val id: ScreeningId,
    val movieId: MovieId,
    val room: Room,
    val startTime: ScreeningStartTime,
    val bookings: List<Booking>,
) {
    init {
        val singleSeats = findSingleSeats(allSeats, isTaken = { it in takenSeats })
        check(singleSeats.isEmpty()) { "Booking of ID $id has single seats: $singleSeats" }
    }

    fun book(booking: UnconfirmedBooking): EitherNel<BookingError, Screening> {
        if (booking.id in bookingIds) return this.right()

        return either {
            accumulateErrors(
                { ensureSeatsExist(booking) },
                { ensureSeatsNotTaken(booking) },
                { ensureNoSingleSeats(booking) },
            )

            copy(bookings = bookings + booking)
        }
    }

    private fun Raise<NonEmptyList<SeatDoesNotExist>>.ensureSeatsExist(booking: Booking) {
        accumulateErrors(booking.seats) { seat ->
            ensure(seat in allSeats) { SeatDoesNotExist(seat) }
        }
    }

    private fun Raise<NonEmptyList<SeatAlreadyTaken>>.ensureSeatsNotTaken(booking: Booking) {
        accumulateErrors(booking.seats) { seat ->
            ensure(seat !in takenSeats) { SeatAlreadyTaken(seat) }
        }
    }

    private fun Raise<NonEmptyList<SingleSeatLeft>>.ensureNoSingleSeats(booking: Booking) {
        val takenSeatsAfterBooking = takenSeats + booking.seats
        val singleSeats = findSingleSeats(allSeats, isTaken = { it in takenSeatsAfterBooking })

        accumulateErrors(singleSeats) { seat ->
            raise(SingleSeatLeft(seat))
        }
    }

    fun cancelExpiredBookings(currentTime: LocalDateTime): Screening {
        val expiredBookings = findExpiredBookings(currentTime)

        val expiredBookingIds: Set<BookingId> = expiredBookings.mapTo(mutableSetOf()) { it.id }
        val nonExpiredBookings = bookings.filter { it.id !in expiredBookingIds }

        val updatedBookings = nonExpiredBookings + expiredBookings
        return copy(bookings = updatedBookings)
    }

    private fun findExpiredBookings(currentTime: LocalDateTime) =
        bookings.filterIsInstance<UnconfirmedBooking>()
            .filter { it.expirationTime.value isBefore currentTime }
            .map { it.expire() }

    fun confirmBooking(
        bookingId: BookingId,
        currentTime: LocalDateTime,
    ): Either<BookingConfirmationError, Screening> =
        either {
            val booking = bookings.find { it.id == bookingId }
            ensureNotNull(booking) { BookingDoesNotExist(bookingId) }

            val confirmedBooking = when (booking) {
                is ConfirmedBooking -> booking
                is UnconfirmedBooking -> booking.confirm(currentTime).bind()
                is ExpiredBooking -> raise(BookingExpired(booking.expirationTime))
            }

            copy(bookings = bookings.replace(booking, confirmedBooking))
        }
}

private val Screening.takenSeats: Set<Seat>
    get() =
        bookings.filter { it is UnconfirmedBooking || it is ConfirmedBooking }
            .flatMapTo(mutableSetOf()) { it.seats }

private val Screening.allSeats: Set<Seat>
    get() = room.seats.toSet()

private val Screening.bookingIds: Set<BookingId>
    get() = bookings.mapTo(mutableSetOf()) { it.id }

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
