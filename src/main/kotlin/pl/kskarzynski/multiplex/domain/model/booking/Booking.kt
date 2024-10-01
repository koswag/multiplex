package pl.kskarzynski.multiplex.domain.model.booking

import arrow.core.Either
import arrow.core.NonEmptyCollection
import arrow.core.raise.either
import arrow.core.raise.ensure
import java.time.LocalDateTime
import pl.kskarzynski.multiplex.domain.model.booking.BookingConfirmationError.BookingExpired
import pl.kskarzynski.multiplex.domain.model.screening.Screening
import pl.kskarzynski.multiplex.domain.model.screening.SeatPlacement
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket
import pl.kskarzynski.multiplex.domain.model.user.UserInfo

sealed interface Booking {
    val id: BookingId
    val userInfo: UserInfo
    val tickets: NonEmptyCollection<Ticket>
    val screening: Screening
    val bookingTime: BookingTime

    data class UnconfirmedBooking(
        override val id: BookingId,
        override val userInfo: UserInfo,
        override val tickets: NonEmptyCollection<Ticket>,
        override val screening: Screening,
        override val bookingTime: BookingTime,
        val totalPrice: BookingPrice,
        val expirationTime: BookingExpirationTime,
    ) : Booking {
        private val seats: List<SeatPlacement>
            get() = tickets.map { it.seatPlacement }

        fun cancel(): CancelledBooking {
            val updatedScreening = screening.cancelBooking(seats)
            return CancelledBooking(id, userInfo, tickets, updatedScreening, bookingTime, expirationTime)
        }

        fun confirm(currTime: LocalDateTime): Either<BookingConfirmationError, ConfirmedBooking> =
            either {
                ensure(currTime.isBefore(expirationTime.value)) { BookingExpired(expirationTime) }
                ConfirmedBooking(id, userInfo, tickets, screening, bookingTime, totalPrice)
            }
    }

    data class CancelledBooking(
        override val id: BookingId,
        override val userInfo: UserInfo,
        override val tickets: NonEmptyCollection<Ticket>,
        override val screening: Screening,
        override val bookingTime: BookingTime,
        val expirationTime: BookingExpirationTime,
    ) : Booking

    data class ConfirmedBooking(
        override val id: BookingId,
        override val userInfo: UserInfo,
        override val tickets: NonEmptyCollection<Ticket>,
        override val screening: Screening,
        override val bookingTime: BookingTime,
        val totalPrice: BookingPrice,
    ) : Booking
}
