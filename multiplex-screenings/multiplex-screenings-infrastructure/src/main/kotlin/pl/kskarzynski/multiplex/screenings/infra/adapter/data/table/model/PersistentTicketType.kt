package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.TicketType

enum class PersistentTicketType {
    ADULT,
    STUDENT,
    CHILD,
    ;

    fun toDomain(): TicketType =
        when (this) {
            ADULT -> TicketType.ADULT
            STUDENT -> TicketType.STUDENT
            CHILD -> TicketType.CHILD
        }

    companion object {
        fun from(ticketType: TicketType): PersistentTicketType =
            when (ticketType) {
                TicketType.ADULT -> ADULT
                TicketType.STUDENT -> STUDENT
                TicketType.CHILD -> CHILD
            }
    }
}
