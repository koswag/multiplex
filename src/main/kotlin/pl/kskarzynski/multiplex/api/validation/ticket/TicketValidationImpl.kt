package pl.kskarzynski.multiplex.api.validation.ticket

import arrow.core.EitherNel
import arrow.core.NonEmptyList
import arrow.core.nel
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.mapOrAccumulate
import arrow.core.raise.zipOrAccumulate
import org.springframework.stereotype.Service
import pl.kskarzynski.multiplex.api.model.TicketDto
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.TicketValidationError
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.TicketValidationError.InvalidRowNumber
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.TicketValidationError.InvalidSeatNumber
import pl.kskarzynski.multiplex.api.validation.BookingValidationError.TicketValidationError.NoTickets
import pl.kskarzynski.multiplex.common.util.extensions.toNonEmptyList
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket
import pl.kskarzynski.multiplex.shared.screening.SeatNumber
import pl.kskarzynski.multiplex.shared.screening.SeatPlacement
import pl.kskarzynski.multiplex.shared.screening.SeatRow

// TODO: Tests
@Service
class TicketValidationImpl : TicketValidation {

    override fun validateTickets(tickets: List<TicketDto>): EitherNel<TicketValidationError, NonEmptyList<Ticket>> =
        either {
            ensure(tickets.isNotEmpty()) { NoTickets.nel() }

            val validTickets = mapOrAccumulate(tickets) { ticket ->
                zipOrAccumulate(
                    { validateRowNumber(ticket.placement.row) },
                    { validateSeatNumber(ticket.placement.number) },
                ) { row, number ->
                    Ticket(
                        type = ticket.type.toModel(),
                        seatPlacement = SeatPlacement(row, number)
                    )
                }
            }

            validTickets.toNonEmptyList()
        }
}

context(Raise<InvalidRowNumber>)
private fun validateRowNumber(rowNumber: Int): SeatRow {
    ensure(rowNumber >= SeatRow.MIN_VALUE) { InvalidRowNumber(rowNumber) }
    return SeatRow(rowNumber)
}

context(Raise<InvalidSeatNumber>)
private fun validateSeatNumber(seatNumber: Int): SeatNumber {
    ensure(seatNumber >= SeatNumber.MIN_VALUE) { InvalidSeatNumber(seatNumber) }
    return SeatNumber(seatNumber)
}
