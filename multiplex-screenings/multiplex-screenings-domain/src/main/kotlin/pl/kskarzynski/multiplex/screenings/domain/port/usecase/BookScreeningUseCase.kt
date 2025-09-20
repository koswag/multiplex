package pl.kskarzynski.multiplex.screenings.domain.port.usecase

import arrow.core.EitherNel
import arrow.core.raise.either
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingError
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.screenings.domain.port.data.ScreeningRepository
import pl.kskarzynski.multiplex.screenings.domain.port.policy.BookingExpirationPolicy
import pl.kskarzynski.multiplex.screenings.domain.port.policy.BookingPricingPolicy
import pl.kskarzynski.multiplex.shared.booking.BookingId

class BookScreeningUseCase(
    private val bookingPricingPolicy: BookingPricingPolicy,
    private val bookingExpirationPolicy: BookingExpirationPolicy,
    private val screeningRepository: ScreeningRepository,
) {
    suspend fun execute(screening: Screening, bookingRequest: BookingRequest): EitherNel<BookingError, UnconfirmedBooking> =
        either {
            val booking = createBooking(bookingRequest, screening)
            val updatedScreening = screening.book(booking).bind()
            screeningRepository.save(updatedScreening)

            booking
        }

    private fun createBooking(bookingRequest: BookingRequest, screening: Screening): UnconfirmedBooking {
        val totalPrice = bookingPricingPolicy.priceBooking(bookingRequest, screening)
        val expirationTime = bookingExpirationPolicy.determineBookingExpirationTime(bookingRequest)

        return UnconfirmedBooking(
            id = BookingId.generate(),
            userInfo = bookingRequest.userInfo,
            tickets = bookingRequest.tickets,
            bookingTime = bookingRequest.bookingTime,
            totalPrice = totalPrice,
            expirationTime = expirationTime,
        )
    }
}
