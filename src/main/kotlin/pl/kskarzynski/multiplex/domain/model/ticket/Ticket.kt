package pl.kskarzynski.multiplex.domain.model.ticket

import pl.kskarzynski.multiplex.shared.room.Seat

data class Ticket(
    val type: TicketType,
    val seatPlacement: Seat,
)
