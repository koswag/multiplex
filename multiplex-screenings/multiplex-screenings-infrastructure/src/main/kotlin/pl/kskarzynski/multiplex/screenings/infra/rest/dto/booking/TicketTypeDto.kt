package pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking

import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.TicketType

enum class TicketTypeDto {
    ADULT,
    STUDENT,
    CHILD,
}

fun TicketTypeDto.toDomain(): TicketType =
    when (this) {
        TicketTypeDto.ADULT -> TicketType.ADULT
        TicketTypeDto.STUDENT -> TicketType.STUDENT
        TicketTypeDto.CHILD -> TicketType.CHILD
    }
