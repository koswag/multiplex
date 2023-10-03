package pl.kskarzynski.multiplex.domain.model.booking

import pl.kskarzynski.multiplex.domain.model.screening.ScreeningInfo
import pl.kskarzynski.multiplex.domain.model.screening.SeatPlacement
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket
import pl.kskarzynski.multiplex.domain.model.user.UserInfo
import java.util.*

@JvmInline
value class BookingId(val value: UUID) {
    companion object {
        fun generate(): BookingId =
            BookingId(UUID.randomUUID())
    }
}

data class Booking(
    val id: BookingId,
    val userInfo: UserInfo,
    val tickets: Collection<Ticket>,
    val screening: ScreeningInfo,
) {
    val seats: Collection<SeatPlacement> =
        tickets.map { it.seatPlacement }
}
