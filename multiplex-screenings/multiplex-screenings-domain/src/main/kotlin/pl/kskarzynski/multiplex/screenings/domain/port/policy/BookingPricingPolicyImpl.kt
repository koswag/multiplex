package pl.kskarzynski.multiplex.screenings.domain.port.policy

import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.shared.booking.BookingPrice
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime
import java.math.BigDecimal
import java.time.DayOfWeek.*

class BookingPricingPolicyImpl : BookingPricingPolicy {

    override fun priceBooking(request: BookingRequest, screening: Screening): BookingPrice {
        val basePrice = BookingPrice(request.tickets.sumOf { it.basePrice.value })
        return increasePriceIfWeekend(screening.startTime, basePrice)
    }

    private fun increasePriceIfWeekend(startTime: ScreeningStartTime, basePrice: BookingPrice): BookingPrice =
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
