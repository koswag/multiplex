package pl.kskarzynski.multiplex.screenings.domain.util

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import java.math.BigDecimal
import java.time.LocalDateTime
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.Ticket
import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.TicketType
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserInfo
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserName
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserSurname
import pl.kskarzynski.multiplex.shared.booking.BookingExpirationTime
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.booking.BookingPrice
import pl.kskarzynski.multiplex.shared.booking.BookingTime
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

val DEFAULT_USER_NAME = UserName("Jane")
val DEFAULT_USER_SURNAME = UserSurname("Doe")
val DEFAULT_USER_INFO = UserInfo(DEFAULT_USER_NAME, DEFAULT_USER_SURNAME)

val DEFAULT_TICKETS =
    nonEmptyListOf(
        Ticket(
            type = TicketType.ADULT,
            seat = Seat(
                SeatRow(1),
                SeatNumber(1),
            )
        )
    )

val DEFAULT_BOOKING_TIME = BookingTime(LocalDateTime.of(2024, 10, 10, 10, 0, 0))
val DEFAULT_EXPIRATION_TIME = BookingExpirationTime(LocalDateTime.of(2024, 10, 10, 10, 15, 0))
val DEFAULT_SCREENING_START_TIME = ScreeningStartTime(LocalDateTime.of(2024, 10, 11, 10, 0, 0))

val DEFAULT_BOOKING_PRICE = BookingPrice(BigDecimal(50))

fun unconfirmedBooking(
    id: BookingId = BookingId.generate(),
    userInfo: UserInfo = DEFAULT_USER_INFO,
    tickets: NonEmptyList<Ticket> = DEFAULT_TICKETS,
    bookingTime: BookingTime = DEFAULT_BOOKING_TIME,
    totalPrice: BookingPrice = DEFAULT_BOOKING_PRICE,
    expirationTime: BookingExpirationTime = DEFAULT_EXPIRATION_TIME,
) = UnconfirmedBooking(id, userInfo, tickets, bookingTime, expirationTime, totalPrice)

fun screening(
    id: ScreeningId = ScreeningId.generate(),
    movieId: MovieId = MovieId.generate(),
    room: Room = room(),
    startTime: ScreeningStartTime = DEFAULT_SCREENING_START_TIME,
    bookings: List<Booking> = emptyList()
) = Screening(id, movieId, room, startTime, bookings)

fun room(
    id: RoomId = RoomId.generate(),
    number: RoomNumber = RoomNumber(1),
    seats: NonEmptyList<Seat> = nonEmptyListOf(seat(1, 1)),
) = Room(id, number, seats)

fun adultTicket(row: Int, number: Int) = Ticket(TicketType.ADULT, seat(row, number))

fun seat(row: Int, number: Int) = Seat(SeatRow(row), SeatNumber(number))
