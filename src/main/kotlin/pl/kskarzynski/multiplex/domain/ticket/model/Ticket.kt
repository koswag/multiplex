package pl.kskarzynski.multiplex.domain.ticket.model

import java.math.BigDecimal

@JvmInline
value class TicketPrice(val value: BigDecimal)

enum class Ticket(val price: TicketPrice) {
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
