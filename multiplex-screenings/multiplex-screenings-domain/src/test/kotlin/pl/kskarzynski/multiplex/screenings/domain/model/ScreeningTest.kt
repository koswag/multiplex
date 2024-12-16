package pl.kskarzynski.multiplex.screenings.domain.model

import arrow.core.nonEmptyListOf
import io.kotest.core.spec.style.FeatureSpec
import pl.kskarzynski.multiplex.common.test.arrow.isLeft
import pl.kskarzynski.multiplex.common.test.arrow.isRight
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingError
import pl.kskarzynski.multiplex.screenings.domain.util.adultTicket
import pl.kskarzynski.multiplex.screenings.domain.util.room
import pl.kskarzynski.multiplex.screenings.domain.util.screening
import pl.kskarzynski.multiplex.screenings.domain.util.seat
import pl.kskarzynski.multiplex.screenings.domain.util.unconfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.util.unconfirmedBookingWithOneTicket
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class ScreeningTest : FeatureSpec({

    feature("Screening booking") {
        scenario("Screening has no bookings") {
            // given:
            val screening = screening(
                bookings = emptyList(),
                room = room(seat(1, 1), seat(1, 2), seat(1, 3)),
            )
            val booking = unconfirmedBookingWithOneTicket(1, 1)

            // when:
            val bookingResult = screening.book(booking)

            // then:
            expectThat(bookingResult).isRight() and {
                get { bookings }.containsExactly(booking)
            }
        }
        
        scenario("Screening has one booking") {
            // given:
            val seats = nonEmptyListOf(seat(1, 1), seat(1, 2), seat(1, 3), seat(1, 4))
            val existingBooking = unconfirmedBookingWithOneTicket(1, 1)
            val screening = screening(
                bookings = listOf(existingBooking),
                room = room(seats = seats),
            )

            val newBooking = unconfirmedBookingWithOneTicket(1, 4)

            // when:
            val bookingResult = screening.book(newBooking)

            // then:
            expectThat(bookingResult).isRight() and {
                get { bookings }.containsExactlyInAnyOrder(existingBooking, newBooking)
            }
        }

        scenario("Screening has multiple bookings") {
            // given:
            val seats = nonEmptyListOf(seat(1, 1), seat(1, 2), seat(1, 3), seat(1, 4), seat(1, 5), seat(1, 6))
            val existingBookings = listOf(
                unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 1))),
                unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 2))),
                unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 3))),
            )
            val screening = screening(
                bookings = existingBookings,
                room = room(seats = seats),
            )

            val newBooking = unconfirmedBookingWithOneTicket(1, 6)

            // when:
            val bookingResult = screening.book(newBooking)

            // then:
            expectThat(bookingResult).isRight() and {
                get { bookings }.containsExactlyInAnyOrder(existingBookings + newBooking)
            }
        }

        scenario("Booking leaving two available seats") {
            // given:
            val seats = nonEmptyListOf(
                seat(1, 1), seat(1, 2), seat(1, 3), seat(1, 4), seat(1, 5),
            )

            val screening = screening(
                room = room(seats = seats),
            )

            val booking = unconfirmedBooking(
                tickets = nonEmptyListOf(
                    adultTicket(1, 1), adultTicket(1, 2), adultTicket(1, 3),
                )
            )

            // when:
            val bookingResult = screening.book(booking)

            // then:
            expectThat(bookingResult).isRight() and {
                get { bookings }.containsExactly(booking)
            }
        }

        scenario("Seat does not exist") {
            // given:
            val seats = nonEmptyListOf(seat(1, 1), seat(1, 2))
            val screening = screening(
                bookings = emptyList(),
                room = room(seats = seats),
            )

            val newBooking = unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 3)))

            // when:
            val bookingResult = screening.book(newBooking)

            // then:
            expectThat(bookingResult).isLeft() and {
                hasSize(1)
                first().isA<BookingError.SeatDoesNotExist>() and {
                    get { seatPlacement } isEqualTo newBooking.tickets.first().seat
                }
            }
        }
        
        scenario("Seat is already taken") {
            // given:
            val seats = nonEmptyListOf(seat(1, 1))
            val screening = screening(
                bookings = listOf(
                    unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 1))),
                ),
                room = room(seats = seats),
            )

            val newBooking = unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 1)))

            // when:
            val bookingResult = screening.book(newBooking)

            // then:
            expectThat(bookingResult).isLeft() and {
                hasSize(1)
                first().isA<BookingError.SeatAlreadyTaken>() and {
                    get { seatPlacement } isEqualTo newBooking.tickets.first().seat
                }
            }
        }

        scenario("Booking would leave single available seats at the beginning of a row") {
            // given:
            val seats = nonEmptyListOf(
                seat(1, 1), seat(1, 2), seat(1, 3), seat(1, 4), seat(1, 5),
            )

            val screening = screening(
                room = room(seats = seats),
            )

            val booking = unconfirmedBookingWithOneTicket(row = 1, seat = 2)

            // when:
            val bookingResult = screening.book(booking)

            // then:
            expectThat(bookingResult).isLeft() and {
                hasSize(1)
                first().isA<BookingError.SingleSeatLeft>() and {
                    get { singleSeatPlacement } isEqualTo seat(1, 1)
                }
            }
        }

        scenario("Booking would leave single available seats at the end of a row") {
            // given:
            val seats = nonEmptyListOf(
                seat(1, 1), seat(1, 2), seat(1, 3), seat(1, 4), seat(1, 5),
            )

            val screening = screening(
                room = room(seats = seats),
            )

            val booking = unconfirmedBookingWithOneTicket(row = 1, seat = 4)

            // when:
            val bookingResult = screening.book(booking)

            // then:
            expectThat(bookingResult).isLeft() and {
                hasSize(1)
                first().isA<BookingError.SingleSeatLeft>() and {
                    get { singleSeatPlacement } isEqualTo seat(1, 5)
                }
            }
        }

        scenario("Booking would leave single available seats in the middle of a row") {
            // given:
            val seats = nonEmptyListOf(
                seat(1, 1), seat(1, 2), seat(1, 3), seat(1, 4), seat(1, 5),
            )

            val screening = screening(
                room = room(seats = seats),
            )

            val booking = unconfirmedBooking(
                tickets = nonEmptyListOf(
                    adultTicket(1, 1), adultTicket(1, 2), adultTicket(1, 4), adultTicket(1, 5),
                )
            )

            // when:
            val bookingResult = screening.book(booking)

            // then:
            expectThat(bookingResult).isLeft() and {
                hasSize(1)
                first().isA<BookingError.SingleSeatLeft>() and {
                    get { singleSeatPlacement } isEqualTo seat(1, 3)
                }
            }
        }

        scenario("Booking with alternating odd seats would leave single available seats") {
            // given:
            val seats = nonEmptyListOf(
                seat(1, 1), seat(1, 2), seat(1, 3), seat(1, 4), seat(1, 5),
            )

            val screening = screening(
                room = room(seats = seats),
            )

            val booking = unconfirmedBooking(
                tickets = nonEmptyListOf(
                    adultTicket(1, 1), adultTicket(1, 3), adultTicket(1, 5),
                )
            )

            // when:
            val bookingResult = screening.book(booking)

            // then:
            expectThat(bookingResult).isLeft() and {
                hasSize(2)
                containsExactlyInAnyOrder(
                    BookingError.SingleSeatLeft(seat(1, 2)),
                    BookingError.SingleSeatLeft(seat(1, 4)),
                )
            }
        }

        scenario("Booking with alternating even seats would leave single available seats") {
            // given:
            val seats = nonEmptyListOf(
                seat(1, 1), seat(1, 2), seat(1, 3), seat(1, 4), seat(1, 5),
            )

            val screening = screening(
                room = room(seats = seats),
            )

            val booking = unconfirmedBooking(
                tickets = nonEmptyListOf(
                    adultTicket(1, 2), adultTicket(1, 4),
                )
            )

            // when:
            val bookingResult = screening.book(booking)

            // then:
            expectThat(bookingResult).isLeft() and {
                hasSize(3)
                containsExactlyInAnyOrder(
                    BookingError.SingleSeatLeft(seat(1, 1)),
                    BookingError.SingleSeatLeft(seat(1, 3)),
                    BookingError.SingleSeatLeft(seat(1, 5)),
                )
            }
        }

        scenario("Booking with all middle seats seats would leave single available seats") {
            // given:
            val seats = nonEmptyListOf(
                seat(1, 1), seat(1, 2), seat(1, 3), seat(1, 4), seat(1, 5),
            )

            val screening = screening(
                room = room(seats = seats),
            )

            val booking = unconfirmedBooking(
                tickets = nonEmptyListOf(
                    adultTicket(1, 2), adultTicket(1, 3), adultTicket(1, 4),
                )
            )

            // when:
            val bookingResult = screening.book(booking)

            // then:
            expectThat(bookingResult).isLeft() and {
                hasSize(2)
                containsExactlyInAnyOrder(
                    BookingError.SingleSeatLeft(seat(1, 1)),
                    BookingError.SingleSeatLeft(seat(1, 5)),
                )
            }
        }
    }

})
