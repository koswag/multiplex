package pl.kskarzynski.multiplex.api.validation

import arrow.core.EitherNel
import arrow.core.raise.either
import arrow.core.raise.zipOrAccumulate
import java.time.Clock
import java.time.LocalDateTime
import org.springframework.stereotype.Service
import pl.kskarzynski.multiplex.api.model.BookingDto
import pl.kskarzynski.multiplex.api.validation.screening.ScreeningValidation
import pl.kskarzynski.multiplex.api.validation.ticket.TicketValidation
import pl.kskarzynski.multiplex.api.validation.user.UserNameValidation
import pl.kskarzynski.multiplex.api.validation.user.UserSurnameValidation
import pl.kskarzynski.multiplex.domain.model.booking.Booking
import pl.kskarzynski.multiplex.domain.model.booking.BookingId
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningId
import pl.kskarzynski.multiplex.domain.model.user.UserInfo

// TODO: Tests
@Service
class BookingValidationImpl(
    private val clock: Clock,
    private val screeningValidation: ScreeningValidation,
    private val userNameValidation: UserNameValidation,
    private val userSurnameValidation: UserSurnameValidation,
    private val ticketValidation: TicketValidation,
) : BookingValidation {

    override suspend fun validateBooking(booking: BookingDto): EitherNel<BookingValidationError, Booking> =
        either {
            val screeningId = ScreeningId(booking.screeningId)
            val bookingTime = LocalDateTime.now(clock)

            zipOrAccumulate(
                { screeningValidation.validateBookedScreening(screeningId, bookingTime).bind() },
                { userNameValidation.validateName(booking.userInfo.name).bind() },
                { userSurnameValidation.validateSurname(booking.userInfo.surname).bind() },
                { ticketValidation.validateTickets(booking.tickets).bind() }
            ) { screening, name, surname, tickets ->
                Booking(
                    id = BookingId.generate(),
                    userInfo = UserInfo(name, surname),
                    tickets = tickets,
                    screening = screening,
                    bookedAt = bookingTime,
                )
            }
        }
}
