package pl.kskarzynski.multiplex.domain.model.ticket

import pl.kskarzynski.multiplex.domain.model.screening.SeatPlacement

data class Ticket(
    val type: TicketType,
    val seatPlacement: SeatPlacement,
)
