package pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket

import java.math.BigDecimal

private const val ADULT_TICKET_PRICE = 25
private const val STUDENT_TICKET_PRICE = 50
private val CHILD_TICKET_PRICE = BigDecimal("12.50")

enum class TicketType(val price: TicketPrice) {
    ADULT(price = TicketPrice(ADULT_TICKET_PRICE)),
    STUDENT(price = TicketPrice(STUDENT_TICKET_PRICE)),
    CHILD(price = TicketPrice(CHILD_TICKET_PRICE)),
}
