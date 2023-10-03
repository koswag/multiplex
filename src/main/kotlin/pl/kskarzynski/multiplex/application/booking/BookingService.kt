package pl.kskarzynski.multiplex.application.booking

import arrow.core.EitherNel
import pl.kskarzynski.multiplex.domain.model.booking.Booking
import pl.kskarzynski.multiplex.domain.model.booking.BookingValidationError
import pl.kskarzynski.multiplex.domain.model.booking.PricedBooking
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningInfo
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningSummary
import pl.kskarzynski.multiplex.domain.repo.BookingRepository
import pl.kskarzynski.multiplex.domain.services.expiration.BookingExpirationPolicy
import pl.kskarzynski.multiplex.domain.services.pricing.BookingPricingPolicy
import pl.kskarzynski.multiplex.domain.services.validation.BookingValidation
import java.time.LocalDateTime

interface BookingService {
    val bookingValidation: BookingValidation
    val bookingPricingPolicy: BookingPricingPolicy
    val bookingExpirationPolicy: BookingExpirationPolicy
    val bookingRepository: BookingRepository

    fun bookScreening(
        currTime: LocalDateTime,
        booking: Booking,
    ): EitherNel<BookingValidationError, PricedBooking> =
        bookingValidation.validateBooking(currTime, booking)
            .map { validBooking ->
                val totalPrice = bookingPricingPolicy(validBooking)
                val expirationDate = bookingExpirationPolicy(currTime)

                PricedBooking(
                    id = booking.id,
                    userInfo = booking.userInfo,
                    screening = mapScreeningSummary(booking.screening),
                    tickets = booking.tickets,
                    totalPrice = totalPrice,
                    expiresAt = expirationDate,
                ).also {
                    bookingRepository.saveBooking(it)
                }
            }

    private fun mapScreeningSummary(screening: ScreeningInfo): ScreeningSummary =
        ScreeningSummary(
            id = screening.id,
            title = screening.title,
            startTime = screening.startTime,
        )
}
