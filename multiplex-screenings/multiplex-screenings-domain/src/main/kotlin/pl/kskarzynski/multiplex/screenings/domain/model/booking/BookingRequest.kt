package pl.kskarzynski.multiplex.screenings.domain.model.booking

import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.Ticket
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserInfo
import pl.kskarzynski.multiplex.shared.booking.BookingTime
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

data class BookingRequest(
    val screeningId: ScreeningId,
    val userInfo: UserInfo,
    val tickets: List<Ticket>,
    val bookingTime: BookingTime,
)
