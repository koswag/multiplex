package pl.kskarzynski.multiplex.screenings.domain.port.usecase

import arrow.core.EitherNel
import arrow.core.raise.either
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingError
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.screenings.domain.model.booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.port.data.ScreeningRepository
import pl.kskarzynski.multiplex.screenings.domain.port.policy.BookingExpirationPolicy
import pl.kskarzynski.multiplex.screenings.domain.port.policy.BookingPricingPolicy
import pl.kskarzynski.multiplex.shared.booking.BookingId

class BookScreeningUseCase(
    private val bookingPricingPolicy: BookingPricingPolicy,
    private val bookingExpirationPolicy: BookingExpirationPolicy,
    private val screeningRepository: ScreeningRepository,
) {
    suspend fun execute(screening: Screening, request: BookingRequest): EitherNel<BookingError, UnconfirmedBooking> =
        either {
            val booking = createBooking(request, screening)
            val updatedScreening = screening.book(booking).bind()
            screeningRepository.save(updatedScreening)

            booking
        }

    private fun createBooking(request: BookingRequest, screening: Screening): UnconfirmedBooking {
        val totalPrice = bookingPricingPolicy.priceBooking(request, screening)
        val expirationTime = bookingExpirationPolicy.determineBookingExpirationTime(request)

        return UnconfirmedBooking(
            id = BookingId.generate(),
            userInfo = request.userInfo,
            tickets = request.tickets,
            bookingTime = request.bookingTime,
            totalPrice = totalPrice,
            expirationTime = expirationTime,
        )
    }
}
