package pl.kskarzynski.multiplex.domain.model.screening

import java.time.LocalDateTime
import java.util.UUID

@JvmInline
value class ScreeningId(val value: UUID) {
    companion object {
        fun generate(): ScreeningId =
            ScreeningId(UUID.randomUUID())
    }
}

@JvmInline
value class MovieTitle(val value: String)

data class ScreeningInfo(
    val id: ScreeningId,
    val title: MovieTitle,
    val startTime: LocalDateTime,
    val room: Room,
) {
    val alreadyTakenSeats: Collection<SeatPlacement> =
        room.seats
            .filter { it.isTaken }
            .map { it.placement }
}
