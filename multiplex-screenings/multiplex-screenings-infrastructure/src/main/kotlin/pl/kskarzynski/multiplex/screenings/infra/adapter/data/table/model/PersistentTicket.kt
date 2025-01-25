package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.Ticket

internal data class PersistentTicket(
    val type: PersistentTicketType,
    val seat: PersistentSeat,
)

internal fun PersistentTicket.toDomain() =
    Ticket(
        type = type.toDomain(),
        seat = seat.toDomain(),
    )

internal fun Ticket.toPersistentTicket() =
    PersistentTicket(
        type = type.toPersistentTicketType(),
        seat = seat.toPersistentSeat(),
    )
