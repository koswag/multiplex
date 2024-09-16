package pl.kskarzynski.multiplex.domain.model.booking

import arrow.core.NonEmptyCollection
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MINUTES
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningInfo
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket
import pl.kskarzynski.multiplex.domain.model.user.UserInfo

data class Booking(
    val id: BookingId,
    val userInfo: UserInfo,
    val tickets: NonEmptyCollection<Ticket>,
    val screening: ScreeningInfo,
    val bookedAt: LocalDateTime,
) {
    init {
        require(MINUTES.between(bookedAt, screening.startTime) >= MIN_TIME_BEFORE_SCREENING_IN_MINUTES) {
            "Screening has to be booked at most $MIN_TIME_BEFORE_SCREENING_IN_MINUTES minutes before screening"
        }
    }

    companion object {
        const val MIN_TIME_BEFORE_SCREENING_IN_MINUTES = 15
    }
}
