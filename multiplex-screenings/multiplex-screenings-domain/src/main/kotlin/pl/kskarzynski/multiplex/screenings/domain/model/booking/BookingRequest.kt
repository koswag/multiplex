package pl.kskarzynski.multiplex.screenings.domain.model.booking

import arrow.core.NonEmptyList
import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.Ticket
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserInfo
import pl.kskarzynski.multiplex.shared.booking.BookingTime
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

data class BookingRequest(
    val screeningId: ScreeningId,
    val userInfo: UserInfo,
    val tickets: NonEmptyList<Ticket>,
    val bookingTime: BookingTime,
)
