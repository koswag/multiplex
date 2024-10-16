package pl.kskarzynski.multiplex.screenings.domain.port.usecase

import arrow.core.EitherNel
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingError
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingError.ScreeningDoesNotExist
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
    suspend fun execute(bookingRequest: BookingRequest): EitherNel<BookingError, UnconfirmedBooking> =
        either {
            val screening = screeningRepository.findScreening(bookingRequest.screeningId)
            ensureNotNull(screening) { ScreeningDoesNotExist(bookingRequest.screeningId).nel() }

            val booking = createBooking(bookingRequest)
            val updatedScreening = screening.book(booking).bind()

            booking
                .also { screeningRepository.saveScreening(updatedScreening) }
        }

    private fun createBooking(bookingRequest: BookingRequest): UnconfirmedBooking {
        val totalPrice = bookingPricingPolicy.priceBooking(bookingRequest)
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
