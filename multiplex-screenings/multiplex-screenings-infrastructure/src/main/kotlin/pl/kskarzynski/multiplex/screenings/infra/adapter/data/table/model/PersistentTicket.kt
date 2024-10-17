package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.Ticket

data class PersistentTicket(
    val type: PersistentTicketType,
    val seat: PersistentSeat,
) {
    fun toDomain() =
        Ticket(
            type = type.toDomain(),
            seat = seat.toDomain(),
        )

    companion object {
        fun from(ticket: Ticket) =
            PersistentTicket(
                type = PersistentTicketType.from(ticket.type),
                seat = PersistentSeat.from(ticket.seat),
            )
    }
}
