package pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket

import java.math.BigDecimal

@JvmInline
value class TicketPrice(val value: BigDecimal) {
    constructor(price: Int) : this(BigDecimal(price))
}
