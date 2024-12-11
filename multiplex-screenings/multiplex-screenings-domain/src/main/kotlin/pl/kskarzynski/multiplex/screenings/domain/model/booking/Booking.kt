package pl.kskarzynski.multiplex.screenings.domain.model.booking

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.ensure
import java.time.LocalDateTime
import pl.kskarzynski.multiplex.common.utils.datetime.isBefore
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingConfirmationError.BookingExpired
import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.Ticket
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserInfo
import pl.kskarzynski.multiplex.shared.booking.BookingConfirmationTime
import pl.kskarzynski.multiplex.shared.booking.BookingExpirationTime
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.booking.BookingPrice
import pl.kskarzynski.multiplex.shared.booking.BookingTime
import pl.kskarzynski.multiplex.shared.room.Seat

sealed interface Booking {
    val id: BookingId
    val userInfo: UserInfo
    val tickets: NonEmptyList<Ticket>
    val bookingTime: BookingTime
    val expirationTime: BookingExpirationTime

    val seats: List<Seat>
        get() = tickets.map { it.seat }

    data class UnconfirmedBooking(
        override val id: BookingId,
        override val userInfo: UserInfo,
        override val tickets: NonEmptyList<Ticket>,
        override val bookingTime: BookingTime,
        override val expirationTime: BookingExpirationTime,
        val totalPrice: BookingPrice,
    ) : Booking {
        fun expire() = ExpiredBooking(id, userInfo, tickets, bookingTime, expirationTime)

        fun confirm(currentTime: LocalDateTime): Either<BookingExpired, ConfirmedBooking> =
            either {
                ensure(currentTime isBefore expirationTime.value) { BookingExpired(expirationTime) }

                ConfirmedBooking(
                    id = id,
                    userInfo = userInfo,
                    tickets = tickets,
                    bookingTime = bookingTime,
                    expirationTime = expirationTime,
                    totalPrice = totalPrice,
                    confirmedAt = BookingConfirmationTime(currentTime),
                )
            }
    }

    data class ExpiredBooking(
        override val id: BookingId,
        override val userInfo: UserInfo,
        override val tickets: NonEmptyList<Ticket>,
        override val bookingTime: BookingTime,
        override val expirationTime: BookingExpirationTime,
    ) : Booking

    data class ConfirmedBooking(
        override val id: BookingId,
        override val userInfo: UserInfo,
        override val tickets: NonEmptyList<Ticket>,
        override val bookingTime: BookingTime,
        override val expirationTime: BookingExpirationTime,
        val totalPrice: BookingPrice,
        val confirmedAt: BookingConfirmationTime,
    ) : Booking
}
