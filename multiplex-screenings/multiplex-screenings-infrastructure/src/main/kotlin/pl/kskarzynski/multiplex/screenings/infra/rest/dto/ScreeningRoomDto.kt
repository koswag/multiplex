@file:UseSerializers(UuidSerializer::class)
@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking
import pl.kskarzynski.multiplex.shared.room.Room
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ScreeningRoomDto(
    val id: Uuid,
    val number: Int,
    val seats: List<ScreeningRoomSeatDto>,
)

fun Screening.toScreeningRoomDto(): ScreeningRoomDto = room.toDto(bookings)

fun Room.toDto(bookings: List<Booking>): ScreeningRoomDto {
    val takenSeats = bookings.flatMap { it.seats }.toSet()

    return ScreeningRoomDto(
        id = id.value,
        number = number.value,
        seats = seats.map { it.toDto(takenSeats) },
    )
}
