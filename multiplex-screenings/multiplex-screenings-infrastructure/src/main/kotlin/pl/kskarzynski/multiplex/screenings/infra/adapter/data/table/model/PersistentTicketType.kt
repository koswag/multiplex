package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.TicketType
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentTicketType.ADULT
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentTicketType.CHILD
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentTicketType.STUDENT

internal enum class PersistentTicketType {
    ADULT,
    STUDENT,
    CHILD,
}

internal fun PersistentTicketType.toDomain(): TicketType =
    when (this) {
        ADULT -> TicketType.ADULT
        STUDENT -> TicketType.STUDENT
        CHILD -> TicketType.CHILD
    }

internal fun TicketType.toPersistentTicketType(): PersistentTicketType =
    when (this) {
        TicketType.ADULT -> ADULT
        TicketType.STUDENT -> STUDENT
        TicketType.CHILD -> CHILD
    }
