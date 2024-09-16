package pl.kskarzynski.multiplex.domain.model.screening

import java.time.LocalDateTime

data class ScreeningInfo(
    val id: ScreeningId,
    val title: MovieTitle,
    val startTime: LocalDateTime,
    val room: ScreeningRoom,
)
