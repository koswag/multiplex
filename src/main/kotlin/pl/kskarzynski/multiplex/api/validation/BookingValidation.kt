package pl.kskarzynski.multiplex.api.validation

import arrow.core.EitherNel
import pl.kskarzynski.multiplex.api.model.BookingDto
import pl.kskarzynski.multiplex.domain.model.booking.BookingRequest

interface BookingValidation {
    suspend fun validateBooking(booking: BookingDto): EitherNel<BookingValidationError, BookingRequest>
}
