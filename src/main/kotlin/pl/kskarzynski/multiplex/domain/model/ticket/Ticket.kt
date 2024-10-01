package pl.kskarzynski.multiplex.domain.model.ticket

import pl.kskarzynski.multiplex.shared.screening.SeatPlacement

data class Ticket(
    val type: TicketType,
    val seatPlacement: SeatPlacement,
)
