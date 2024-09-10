package pl.kskarzynski.multiplex.api.validation.ticket

import arrow.core.EitherNel
import arrow.core.NonEmptyList
import pl.kskarzynski.multiplex.api.model.TicketDto
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.TicketValidationError
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket

interface TicketValidation {
    fun validateTickets(tickets: List<TicketDto>): EitherNel<TicketValidationError, NonEmptyList<Ticket>>
}
