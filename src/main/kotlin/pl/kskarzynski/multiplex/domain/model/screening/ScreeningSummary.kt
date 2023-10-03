package pl.kskarzynski.multiplex.domain.model.screening

import java.time.LocalDateTime

data class ScreeningSummary(
    val id: ScreeningId,
    val title: MovieTitle,
    val startTime: LocalDateTime,
)
