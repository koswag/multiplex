package pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking

import arrow.core.EitherNel
import arrow.core.raise.either
import arrow.core.raise.ensure
import kotlinx.serialization.Serializable
import pl.kskarzynski.multiplex.common.utils.arrow.accumulateErrors
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto.IllegalSeatNumber
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto.IllegalSeatRow
import pl.kskarzynski.multiplex.shared.room.Seat
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

@Serializable
data class BookingSeatDto(
    val row: Int,
    val number: Int,
)

fun BookingSeatDto.toDomain(): EitherNel<BookingValidationErrorDto, Seat> =
    either {
        accumulateErrors(
            { ensure(row >= SeatRow.MIN_VALUE) { IllegalSeatRow(row) } },
            { ensure(number >= SeatNumber.MIN_VALUE) { IllegalSeatNumber(number) } },
        )

        Seat(SeatRow(row), SeatNumber(number))
    }

fun Seat.toDto() =
    BookingSeatDto(
        row = row.value,
        number = number.value,
    )
