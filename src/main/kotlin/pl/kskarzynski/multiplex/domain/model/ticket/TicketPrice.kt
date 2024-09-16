package pl.kskarzynski.multiplex.domain.model.ticket

import java.math.BigDecimal

@JvmInline
value class TicketPrice(val value: BigDecimal) {
    constructor(price: Int) : this(BigDecimal(price))
}
