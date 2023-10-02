package pl.kskarzynski.multiplex.domain.model.screening

import java.time.LocalDateTime

data class ScreeningListItem(
    val title: MovieTitle,
    val time: LocalDateTime,
)
