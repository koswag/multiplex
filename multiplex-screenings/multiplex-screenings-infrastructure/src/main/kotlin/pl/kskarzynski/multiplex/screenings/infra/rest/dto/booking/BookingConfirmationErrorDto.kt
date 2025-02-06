@file:UseSerializers(UuidSerializer::class, LocalDateTimeSerializer::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.LocalDateTimeSerializer
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingConfirmationError
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

@Serializable
sealed interface BookingConfirmationErrorDto {

    @Serializable
    data class BookingExpired(
        val screeningId: UUID,
        val bookingId: UUID,
        val expiredAt: LocalDateTime,
    ) : BookingConfirmationErrorDto

    @Serializable
    data class BookingDoesNotExist(val screeningId: UUID, val bookingId: UUID) : BookingConfirmationErrorDto
}

fun BookingConfirmationError.toDto(screeningId: ScreeningId, bookingId: BookingId): BookingConfirmationErrorDto =
    when (this) {
        is BookingConfirmationError.BookingExpired ->
            BookingConfirmationErrorDto.BookingExpired(
                screeningId = screeningId.value,
                bookingId = bookingId.value,
                expiredAt = expiredAt.value,
            )

        is BookingConfirmationError.BookingDoesNotExist ->
            BookingConfirmationErrorDto.BookingDoesNotExist(
                screeningId = screeningId.value,
                bookingId = bookingId.value,
            )
    }
