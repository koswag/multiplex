package pl.kskarzynski.multiplex.screenings.domain.port.policy

import java.math.BigDecimal
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDateTime
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.shared.booking.BookingPrice

class BookingPricingPolicyImpl : BookingPricingPolicy {

    override fun priceBooking(bookingRequest: BookingRequest, screening: Screening): BookingPrice {
        val basePrice = BookingPrice(bookingRequest.tickets.sumOf { it.basePrice.value })
        return increasePriceIfWeekend(screening.startTime.value, basePrice)
    }

    private fun increasePriceIfWeekend(startTime: LocalDateTime, basePrice: BookingPrice): BookingPrice =
        when (startTime.dayOfWeek) {
            FRIDAY if startTime.hour > FRIDAY_WEEKEND_START_HOUR -> basePrice + WEEKEND_PRICE_DIFF
            SATURDAY -> basePrice + WEEKEND_PRICE_DIFF
            SUNDAY if startTime.hour < SUNDAY_WEEKEND_END_HOUR -> basePrice + WEEKEND_PRICE_DIFF
            else -> basePrice
        }

    companion object {
        private val WEEKEND_PRICE_DIFF = BigDecimal(4)
        private const val FRIDAY_WEEKEND_START_HOUR = 14
        private const val SUNDAY_WEEKEND_END_HOUR = 23
    }
}
