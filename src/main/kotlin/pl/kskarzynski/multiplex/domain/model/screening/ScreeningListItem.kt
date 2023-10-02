package pl.kskarzynski.multiplex.domain.model.screening

import java.time.LocalDateTime

data class ScreeningListItem(
    val id: ScreeningId,
    val title: MovieTitle,
    val time: LocalDateTime,
)
