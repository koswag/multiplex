@file:UseSerializers(UuidSerializer::class, LocalDateTimeSerializer::class)
@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.LocalDateTimeSerializer
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingConfirmationError
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
sealed interface BookingConfirmationErrorDto {

    @Serializable
    data class BookingExpired(
        val screeningId: Uuid,
        val bookingId: Uuid,
        val expiredAt: LocalDateTime,
    ) : BookingConfirmationErrorDto

    @Serializable
    data class BookingDoesNotExist(val screeningId: Uuid, val bookingId: Uuid) : BookingConfirmationErrorDto
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
