package pl.kskarzynski.multiplex.domain.model.booking

import pl.kskarzynski.multiplex.domain.model.screening.ScreeningInfo
import pl.kskarzynski.multiplex.domain.model.screening.SeatPlacement
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket
import pl.kskarzynski.multiplex.domain.model.user.UserInfo

data class Booking(
    val userInfo: UserInfo,
    val tickets: Collection<Ticket>,
    val screening: ScreeningInfo,
) {
    val seats: Collection<SeatPlacement> =
        tickets.map { it.seatPlacement }
}
