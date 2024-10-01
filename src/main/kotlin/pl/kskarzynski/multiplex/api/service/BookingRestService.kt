package pl.kskarzynski.multiplex.api.service

import arrow.core.EitherNel
import arrow.core.raise.either
import org.springframework.stereotype.Service
import pl.kskarzynski.multiplex.api.model.BookingDto
import pl.kskarzynski.multiplex.api.validation.BookingValidation
import pl.kskarzynski.multiplex.api.validation.BookingValidationError
import pl.kskarzynski.multiplex.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.domain.model.screening.BookingError
import pl.kskarzynski.multiplex.domain.usecase.BookScreeningUseCase

typealias BookingResult =
    EitherNel<
        BookingValidationError,
        EitherNel<BookingError, UnconfirmedBooking>
    >

@Service
class BookingRestService(
    private val bookingValidation: BookingValidation,
    private val bookScreeningUseCase: BookScreeningUseCase,
) {
    suspend fun bookScreening(bookingDto: BookingDto): BookingResult =
        either {
            val booking = bookingValidation.validateBooking(bookingDto).bind()

            either {
                bookScreeningUseCase.execute(booking).bind()
            }
        }
}
