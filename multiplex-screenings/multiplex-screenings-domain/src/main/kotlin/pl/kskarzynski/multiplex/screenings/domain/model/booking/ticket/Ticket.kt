package pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket

import pl.kskarzynski.multiplex.shared.room.Seat

data class Ticket(
    val type: TicketType,
    val seat: Seat,
) {
    val basePrice: TicketPrice
        get() = type.price
}
