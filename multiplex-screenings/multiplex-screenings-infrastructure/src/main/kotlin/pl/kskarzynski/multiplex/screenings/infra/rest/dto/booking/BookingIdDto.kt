@file:UseSerializers(UuidSerializer::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking

import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.shared.booking.BookingId

@Serializable
data class BookingIdDto(
    val bookingId: UUID,
)

fun BookingId.toDto() = BookingIdDto(value)
