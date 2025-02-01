package pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking

import arrow.core.EitherNel
import arrow.core.raise.either
import kotlinx.serialization.Serializable
import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.Ticket

@Serializable
data class TicketDto(
    val type: TicketTypeDto,
    val seat: BookingSeatDto,
)

fun TicketDto.toDomain(): EitherNel<BookingValidationErrorDto, Ticket> =
    either {
        val validSeat = seat.toDomain().bind()

        Ticket(
            type = type.toDomain(),
            seat = validSeat,
        )
    }

