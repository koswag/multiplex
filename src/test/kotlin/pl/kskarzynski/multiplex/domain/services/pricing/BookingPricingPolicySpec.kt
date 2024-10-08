package pl.kskarzynski.multiplex.domain.services.pricing

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import java.time.LocalDateTime
import pl.kskarzynski.multiplex.common.utils.arrow.toNonEmptyList
import pl.kskarzynski.multiplex.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.domain.model.screening.Screening
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningRoom
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket
import pl.kskarzynski.multiplex.domain.model.ticket.TicketType
import pl.kskarzynski.multiplex.domain.model.user.UserInfo
import pl.kskarzynski.multiplex.domain.model.user.UserName
import pl.kskarzynski.multiplex.domain.model.user.UserSurname
import pl.kskarzynski.multiplex.domain.policy.BookingPricingPolicy
import pl.kskarzynski.multiplex.shared.booking.BookingTime
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

class BookingPricingPolicySpec : FeatureSpec({

    val pricingPolicy = BookingPricingPolicy()

    feature("Booking pricing policy") {
        scenario("Regular booking with various tickets") {
            checkAll(
                Arb.list(
                    gen = Arb.enum<TicketType>(),
                    range = 1..20,
                )
            ) { ticketTypes ->
                // given:
                val booking = bookingOf(ticketTypes = ticketTypes)

                // when:
                val price = pricingPolicy.priceBooking(booking)

                // then:
                price.value shouldBe
                    ticketTypes.sumOf { it.price.value }
            }
        }
    }

})

private fun bookingOf(ticketTypes: Collection<TicketType>): BookingRequest =
    BookingRequest(
        userInfo = UserInfo(UserName("Andrzej"), UserSurname("Kowalski")),
        tickets =
            ticketTypes
                .mapIndexed { i, type ->
                    Ticket(
                        type = type,
                        seatPlacement = Seat(SeatRow(1), SeatNumber(i)),
                    )
                }
                .toNonEmptyList(),
        screening =
            Screening(
                id = ScreeningId.generate(),
                movieId = MovieId.generate(),
                startTime = LocalDateTime.of(2023, 10, 3, 16, 30),
                room = ScreeningRoom(RoomId.generate(), RoomNumber(1), seats = emptyList()),
            ),
        bookingTime = BookingTime(LocalDateTime.of(2023, 10, 3, 16, 30)),
    )
