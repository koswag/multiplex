package pl.kskarzynski.multiplex.api.model

import pl.kskarzynski.multiplex.domain.model.ticket.TicketType

enum class TicketTypeDto {
    ADULT,
    STUDENT,
    CHILD,
    ;

    fun toModel(): TicketType =
        when (this) {
            ADULT -> TicketType.ADULT
            STUDENT -> TicketType.STUDENT
            CHILD -> TicketType.CHILD
        }
}
