package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.Ticket

data class PersistentTicket(
    val type: PersistentTicketType,
    val seat: PersistentSeat,
)

fun PersistentTicket.toDomain() =
    Ticket(
        type = type.toDomain(),
        seat = seat.toDomain(),
    )

fun Ticket.toPersistentTicket() =
    PersistentTicket(
        type = type.toPersistentTicketType(),
        seat = seat.toPersistentSeat(),
    )
