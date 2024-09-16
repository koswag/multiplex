package pl.kskarzynski.multiplex.domain.usecase

import arrow.core.EitherNel
import arrow.core.raise.either
import pl.kskarzynski.multiplex.domain.model.booking.Booking
import pl.kskarzynski.multiplex.domain.model.booking.PricedBooking
import pl.kskarzynski.multiplex.domain.model.screening.BookingError
import pl.kskarzynski.multiplex.domain.policy.BookingExpirationPolicy
import pl.kskarzynski.multiplex.domain.policy.BookingPricingPolicy
import pl.kskarzynski.multiplex.domain.repo.BookingRepository

class BookScreeningUseCase(
    private val bookingPricingPolicy: BookingPricingPolicy,
    private val bookingExpirationPolicy: BookingExpirationPolicy,
    private val bookingRepository: BookingRepository,
) {
    suspend fun execute(booking: Booking): EitherNel<BookingError, PricedBooking> =
        either {
            val booked = booking.bookSeats().bind()

            priceBooking(booked)
                .also { bookingRepository.saveBooking(it) }
        }

    private fun priceBooking(booking: Booking): PricedBooking {
        val totalPrice = bookingPricingPolicy.priceBooking(booking)
        val expirationTime = bookingExpirationPolicy.determineBookingExpirationTime(booking)

        return PricedBooking(
            booking = booking,
            totalPrice = totalPrice,
            expiresAt = expirationTime,
        )
    }
}
