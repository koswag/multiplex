@file:UseSerializers(NonEmptyListSerializer::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking

import arrow.core.EitherNel
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.zipOrAccumulate
import arrow.core.serialization.NonEmptyListSerializer
import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.screenings.domain.model.booking.BookingRequest
import pl.kskarzynski.multiplex.shared.booking.BookingTime
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

@Serializable
data class BookingRequestDto(
    val userInfo: UserInfoDto,
    val tickets: NonEmptyList<TicketDto>,
)

fun BookingRequestDto.toDomain(
    screeningId: ScreeningId,
    currentTime: LocalDateTime,
): EitherNel<BookingValidationErrorDto, BookingRequest> =
    either {
        zipOrAccumulate(
            { userInfo.toDomain().bind() },
            { mapOrAccumulate(tickets) { it.toDomain().bind() } },
        ) { validUserInfo, validTickets ->
            BookingRequest(
                screeningId = screeningId,
                userInfo = validUserInfo,
                tickets = validTickets,
                bookingTime = BookingTime(currentTime),
            )
        }
    }
