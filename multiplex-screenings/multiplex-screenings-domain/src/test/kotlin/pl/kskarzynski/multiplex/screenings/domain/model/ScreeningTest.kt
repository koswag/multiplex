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
            val screening = screening(bookings = emptyList())
            val booking = unconfirmedBooking()

            // when:
            val bookingResult = screening.book(booking)

            // then:
            expectThat(bookingResult).isRight() and {
                get { bookings }.containsExactly(booking)
            }
        }
        
        scenario("Screening has one booking") {
            // given:
            val seats = nonEmptyListOf(seat(1, 1), seat(1, 2))
            val existingBooking = unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 1)))
            val screening = screening(
                bookings = listOf(existingBooking),
                room = room(seats = seats),
            )

            val newBooking = unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 2)))

            // when:
            val bookingResult = screening.book(newBooking)

            // then:
            expectThat(bookingResult).isRight() and {
                get { bookings }.containsExactlyInAnyOrder(existingBooking, newBooking)
            }
        }

        scenario("Screening has multiple bookings") {
            // given:
            val seats = nonEmptyListOf(seat(1, 1), seat(1, 2), seat(1, 3), seat(1, 4))
            val existingBookings = listOf(
                unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 1))),
                unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 2))),
                unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 3))),
            )
            val screening = screening(
                bookings = existingBookings,
                room = room(seats = seats),
            )

            val newBooking = unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 4)))

            // when:
            val bookingResult = screening.book(newBooking)

            // then:
            expectThat(bookingResult).isRight() and {
                get { bookings }.containsExactlyInAnyOrder(existingBookings + newBooking)
            }
        }

        scenario("Seat does not exist") {
            // given:
            val seats = nonEmptyListOf(seat(1, 1))
            val screening = screening(
                bookings = emptyList(),
                room = room(seats = seats),
            )

            val newBooking = unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 2)))

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
                    unconfirmedBooking(tickets = nonEmptyListOf(adultTicket(1, 1)))
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

        scenario("Booking would leave single available seats") {

        }
    }

})
