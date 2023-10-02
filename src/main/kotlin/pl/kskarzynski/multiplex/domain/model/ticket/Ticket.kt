package pl.kskarzynski.multiplex.domain.model.ticket

import pl.kskarzynski.multiplex.domain.model.screening.SeatPlacement
import java.math.BigDecimal

@JvmInline
value class TicketPrice(val value: BigDecimal)

enum class TicketType(val price: TicketPrice) {
    Adult(
        price = TicketPrice(BigDecimal("25.00"))
    ),
    Student(
        price = TicketPrice(BigDecimal("18.00"))
    ),
    Child(
        price = TicketPrice(BigDecimal("12.50"))
    ),
}

data class Ticket(
    val type: TicketType,
    val seatPlacement: SeatPlacement,
)
