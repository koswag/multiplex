package pl.kskarzynski.multiplex.shared.booking

import java.math.BigDecimal

@JvmInline
value class BookingPrice private constructor(val value: BigDecimal) {

    operator fun plus(amount: BigDecimal): BookingPrice {
        require(amount > BigDecimal.ZERO) { "Amount must be positive, but was $amount." }
        return BookingPrice(value + amount)
    }

    companion object {
        operator fun invoke(value: BigDecimal): BookingPrice {
            require(value >= BigDecimal.ZERO) { "Booking price cannot be negative" }
            return BookingPrice(value.stripTrailingZeros())
        }
    }
}
