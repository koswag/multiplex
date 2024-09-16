package pl.kskarzynski.multiplex.domain.model.screening

import java.time.LocalDateTime

data class Screening(
    val id: ScreeningId,
    val startTime: LocalDateTime,
    val room: ScreeningRoom,
)
