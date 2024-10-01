package pl.kskarzynski.multiplex.domain.model.booking

import arrow.core.EitherNel
import arrow.core.NonEmptyCollection
import arrow.core.raise.either
import java.time.temporal.ChronoUnit.MINUTES
import pl.kskarzynski.multiplex.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.domain.model.screening.BookingError
import pl.kskarzynski.multiplex.domain.model.screening.Screening
import pl.kskarzynski.multiplex.domain.model.screening.SeatPlacement
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket
import pl.kskarzynski.multiplex.domain.model.user.UserInfo

data class BookingRequest(
    val userInfo: UserInfo,
    val tickets: NonEmptyCollection<Ticket>,
    val screening: Screening,
    val bookingTime: BookingTime,
) {
    init {
        require(MINUTES.between(bookingTime.value, screening.startTime) >= MIN_TIME_BEFORE_SCREENING_IN_MINUTES) {
            "Screening has to be booked at most $MIN_TIME_BEFORE_SCREENING_IN_MINUTES minutes before screening"
        }
    }

    private val seats: List<SeatPlacement>
        get() = tickets.map { it.seatPlacement }

    fun book(
        price: BookingPrice,
        expirationTime: BookingExpirationTime,
    ): EitherNel<BookingError, UnconfirmedBooking> =
        either {
            val screeningWithBookedSeats = screening.bookSeats(seats).bind()

            UnconfirmedBooking(
                id = BookingId.generate(),
                userInfo = userInfo,
                tickets = tickets,
                screening = screeningWithBookedSeats,
                bookingTime = bookingTime,
                totalPrice = price,
                expirationTime = expirationTime,
            )
        }

    companion object {
        const val MIN_TIME_BEFORE_SCREENING_IN_MINUTES = 15
    }
}
