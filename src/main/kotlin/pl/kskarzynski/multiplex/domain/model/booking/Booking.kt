package pl.kskarzynski.multiplex.domain.model.booking

import arrow.core.EitherNel
import arrow.core.NonEmptyCollection
import arrow.core.raise.either
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MINUTES
import pl.kskarzynski.multiplex.domain.model.screening.BookingError
import pl.kskarzynski.multiplex.domain.model.screening.Screening
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket
import pl.kskarzynski.multiplex.domain.model.user.UserInfo

data class Booking(
    val id: BookingId,
    val userInfo: UserInfo,
    val tickets: NonEmptyCollection<Ticket>,
    val screening: Screening,
    val bookedAt: LocalDateTime,
) {
    init {
        require(MINUTES.between(bookedAt, screening.startTime) >= MIN_TIME_BEFORE_SCREENING_IN_MINUTES) {
            "Screening has to be booked at most $MIN_TIME_BEFORE_SCREENING_IN_MINUTES minutes before screening"
        }
    }

    fun bookSeats(): EitherNel<BookingError, Booking> =
        either {
            val seatPlacements = tickets.map { it.seatPlacement }
            val updatedScreening = screening.bookSeats(seatPlacements).bind()

            copy(screening = updatedScreening)
        }

    companion object {
        const val MIN_TIME_BEFORE_SCREENING_IN_MINUTES = 15
    }
}
