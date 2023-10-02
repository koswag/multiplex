package pl.kskarzynski.multiplex.domain.model.screening

import java.time.LocalDateTime
import java.util.UUID

@JvmInline
value class ScreeningId(val value: UUID)

@JvmInline
value class MovieTitle(val value: String)

data class ScreeningInfo(
    val id: ScreeningId,
    val title: MovieTitle,
    val time: LocalDateTime,
    val room: Room,
)
