package pl.kskarzynski.multiplex.shared.booking

import java.math.BigDecimal

@JvmInline
value class BookingPrice private constructor(val value: BigDecimal) {
    companion object {
        operator fun invoke(value: BigDecimal) = BookingPrice(value.stripTrailingZeros())
    }
}
