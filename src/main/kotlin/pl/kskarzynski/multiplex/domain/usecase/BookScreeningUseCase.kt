package pl.kskarzynski.multiplex.domain.usecase

import arrow.core.EitherNel
import arrow.core.raise.either
import pl.kskarzynski.multiplex.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.domain.model.screening.BookingError
import pl.kskarzynski.multiplex.domain.policy.BookingExpirationPolicy
import pl.kskarzynski.multiplex.domain.policy.BookingPricingPolicy
import pl.kskarzynski.multiplex.domain.repo.BookingRepository

class BookScreeningUseCase(
    private val bookingPricingPolicy: BookingPricingPolicy,
    private val bookingExpirationPolicy: BookingExpirationPolicy,
    private val bookingRepository: BookingRepository,
) {
    suspend fun execute(bookingRequest: BookingRequest): EitherNel<BookingError, UnconfirmedBooking> =
        either {
            val totalPrice = bookingPricingPolicy.priceBooking(bookingRequest)
            val expirationTime = bookingExpirationPolicy.determineBookingExpirationTime(bookingRequest)

            val booking = bookingRequest.book(totalPrice, expirationTime).bind()
            booking.also { bookingRepository.saveBooking(it) }
        }
}
