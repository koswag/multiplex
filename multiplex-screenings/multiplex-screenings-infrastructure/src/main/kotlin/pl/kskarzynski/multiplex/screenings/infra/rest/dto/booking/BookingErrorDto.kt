@file:UseSerializers(NonEmptyListSerializer::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking

import arrow.core.serialization.NonEmptyListSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingError

@Serializable
sealed interface BookingErrorDto {

    @Serializable
    data class SeatDoesNotExist(val seat: BookingSeatDto) : BookingErrorDto

    @Serializable
    data class SeatAlreadyTaken(val seat: BookingSeatDto) : BookingErrorDto

    @Serializable
    data class SingleSeatLeft(val singleSeat: BookingSeatDto) : BookingErrorDto
}

fun BookingError.toDto(): BookingErrorDto =
    when (this) {
        is BookingError.SeatAlreadyTaken -> BookingErrorDto.SeatAlreadyTaken(seat.toDto())
        is BookingError.SeatDoesNotExist -> BookingErrorDto.SeatDoesNotExist(seat.toDto())
        is BookingError.SingleSeatLeft -> BookingErrorDto.SingleSeatLeft(singleSeat.toDto())
    }
