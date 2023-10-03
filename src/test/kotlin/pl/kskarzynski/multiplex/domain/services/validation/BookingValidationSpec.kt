package pl.kskarzynski.multiplex.domain.services.validation

import arrow.core.Either.Right
import arrow.core.getOrElse
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import pl.kskarzynski.multiplex.domain.model.booking.Booking
import pl.kskarzynski.multiplex.domain.model.booking.BookingValidationError.*
import pl.kskarzynski.multiplex.domain.model.booking.ValidBooking
import pl.kskarzynski.multiplex.domain.model.screening.*
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket
import pl.kskarzynski.multiplex.domain.model.ticket.TicketType.Adult
import pl.kskarzynski.multiplex.domain.model.user.UserInfo
import pl.kskarzynski.multiplex.domain.services.validation.BookingValidation.Companion.MIN_TIME_BEFORE_SCREENING_IN_MINUTES
import java.time.LocalDateTime

private object TestBookingValidation : BookingValidation

class BookingValidationSpec : FeatureSpec({

    val testStartTime = LocalDateTime.of(2023, 10, 2, 13, 30)

    val validCurrentTime = testStartTime.minusMinutes((MIN_TIME_BEFORE_SCREENING_IN_MINUTES + 100).toLong())
    val invalidCurrentTime = testStartTime.minusMinutes((MIN_TIME_BEFORE_SCREENING_IN_MINUTES - 5).toLong())

    val testScreening = screeningOf(
        startTime = testStartTime,
        seatOf(1, 1), seatOf(1, 2), seatOf(1, 3), seatOf(1, 4),
        seatOf(2, 1), seatOf(2, 2), seatOf(2, 3, isTaken = true), seatOf(2, 4, isTaken = true),
    )

    val testUser = userOf("Andrzej", "Kowalski")

    fun bookingOf(vararg seats: SeatPlacement): Booking =
        Booking(
            userInfo = testUser,
            tickets = listOf(*seats)
                .map { Ticket(Adult, it) },
            screening = testScreening,
        )

    feature("Booking validation") {
        scenario("Booking is valid") {
            forAll(
                row(listOf(seatPlacementOf(1, 1))),
                row(listOf(seatPlacementOf(1, 4))),
                row(listOf(seatPlacementOf(1, 1), seatPlacementOf(1, 2))),
                row(listOf(seatPlacementOf(1, 3), seatPlacementOf(1, 4))),
                row(listOf(seatPlacementOf(2, 1), seatPlacementOf(2, 2))),
                row(
                    listOf(
                        seatPlacementOf(1, 1),
                        seatPlacementOf(1, 2),
                        seatPlacementOf(1, 3),
                        seatPlacementOf(1, 4),
                    )
                ),
            ) { bookedSeats ->
                // given:
                val booking = bookingOf(*bookedSeats.toTypedArray())

                // when:
                val validationResult = TestBookingValidation.validateBooking(validCurrentTime, booking)

                // then:
                validationResult shouldBe Right(ValidBooking(booking))
            }
        }

        scenario("Too late for booking") {
            // given:
            val currTime = invalidCurrentTime
            val booking = bookingOf(seatPlacementOf(1, 1))

            // when:
            val validationResult = TestBookingValidation.validateBooking(currTime, booking)

            // then:
            validationResult.isRight() shouldBe false
            validationResult.onLeft { errors ->
                errors shouldContain TooLateForBooking
            }
        }

        scenario("No tickets") {
            // given:
            val booking = bookingOf()

            // when:
            val validationResult = TestBookingValidation.validateBooking(validCurrentTime, booking)

            // then:
            validationResult.isRight() shouldBe false
            validationResult.onLeft { errors ->
                errors shouldContain NoTickets
            }
        }

        scenario("Non-existent seats") {
            // given:
            val booking = bookingOf(
                seatPlacementOf(1, 1),
                seatPlacementOf(3, 1),
                seatPlacementOf(3, 2),
            )

            // when:
            val validationResult = TestBookingValidation.validateBooking(validCurrentTime, booking)

            // then:
            validationResult.isRight() shouldBe false
            validationResult.onLeft { errors ->
                errors.shouldHaveSize(1)

                val error = errors.first()
                    .shouldBeInstanceOf<NonExistentSeats>()

                error.seats shouldContainAll listOf(
                    seatPlacementOf(3, 1),
                    seatPlacementOf(3, 2),
                )
            }
        }

        scenario("Seats already taken") {
            // given:
            val booking = bookingOf(
                seatPlacementOf(1, 1),
                seatPlacementOf(2, 3),
                seatPlacementOf(2, 4),
            )

            // when:
            val validationResult = TestBookingValidation.validateBooking(validCurrentTime, booking)

            // then:
            validationResult.isRight() shouldBe false
            validationResult.onLeft { errors ->
                errors.shouldHaveSize(1)

                val error = errors.first()
                    .shouldBeInstanceOf<SeatsAlreadyTaken>()

                error.seats shouldContainAll listOf(
                    seatPlacementOf(2, 3),
                    seatPlacementOf(2, 4),
                )
            }
        }

        scenario("Single seats left") {
            forAll(
                row(listOf(seatPlacementOf(2, 1)), listOf(seatPlacementOf(2, 2))),
                row(listOf(seatPlacementOf(2, 2)), listOf(seatPlacementOf(2, 1))),
                row(
                    listOf(seatPlacementOf(1, 1), seatPlacementOf(1, 2), seatPlacementOf(1, 3)),
                    listOf(seatPlacementOf(1, 4)),
                ),
                row(
                    listOf(seatPlacementOf(1, 1), seatPlacementOf(1, 2), seatPlacementOf(1, 4)),
                    listOf(seatPlacementOf(1, 3)),
                ),
                row(
                    listOf(seatPlacementOf(1, 1), seatPlacementOf(1, 3), seatPlacementOf(1, 4)),
                    listOf(seatPlacementOf(1, 2)),
                ),
                row(
                    listOf(seatPlacementOf(1, 2), seatPlacementOf(1, 3), seatPlacementOf(1, 4)),
                    listOf(seatPlacementOf(1, 1)),
                ),
                row(
                    listOf(seatPlacementOf(1, 2), seatPlacementOf(1, 3)),
                    listOf(seatPlacementOf(1, 1), seatPlacementOf(1, 4)),
                ),
                row(
                    listOf(seatPlacementOf(1, 2)),
                    listOf(seatPlacementOf(1, 1)),
                ),
                row(
                    listOf(seatPlacementOf(1, 3)),
                    listOf(seatPlacementOf(1, 4)),
                ),
            ) { bookedSeats, expectedSingleSeats ->
                // given:
                val booking = bookingOf(*bookedSeats.toTypedArray())

                // when:
                val validationResult = TestBookingValidation.validateBooking(validCurrentTime, booking)

                // then:
                validationResult.isRight() shouldBe false
                validationResult.onLeft { errors ->
                    errors.shouldHaveSize(1)

                    val error = errors.first()
                        .shouldBeInstanceOf<SingleSeatsLeft>()

                    error.seats shouldContainAll expectedSingleSeats
                }
            }
        }
    }

})

private fun screeningOf(startTime: LocalDateTime, vararg seats: Seat): ScreeningInfo =
    ScreeningInfo(
        id = ScreeningId.generate(),
        title = MovieTitle("Movie Title"),
        startTime = startTime,
        room = Room(
            number = RoomNumber(10),
            seats = listOf(*seats),
        )
    )

private fun seatOf(row: Int, number: Int, isTaken: Boolean = false): Seat =
    Seat(
        placement = seatPlacementOf(row, number),
        isTaken = isTaken,
    )

private fun seatPlacementOf(row: Int, number: Int): SeatPlacement =
    SeatPlacement(SeatRow(row), SeatNumber(number))

private fun userOf(name: String, surname: String): UserInfo =
    UserInfo(name, surname)
        .getOrElse { throw IllegalArgumentException(it.joinToString()) }
