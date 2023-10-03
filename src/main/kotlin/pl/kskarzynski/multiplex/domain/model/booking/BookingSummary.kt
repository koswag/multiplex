package pl.kskarzynski.multiplex.domain.model.booking

import pl.kskarzynski.multiplex.domain.model.screening.ScreeningSummary
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket
import pl.kskarzynski.multiplex.domain.model.user.UserInfo
import java.math.BigDecimal
import java.time.LocalDateTime

@JvmInline
value class BookingPrice(val value: BigDecimal)

data class BookingSummary(
    val userInfo: UserInfo,
    val tickets: Collection<Ticket>,
    val screening: ScreeningSummary,
    val totalPrice: BookingPrice,
    val expiresAt: LocalDateTime,
)
