package pl.kskarzynski.multiplex.domain.model.screening

import java.time.LocalDateTime

@JvmInline
value class MovieTitle(val value: String)

data class ScreeningInfo(
    val title: MovieTitle,
    val time: LocalDateTime,
    val room: Room,
)
