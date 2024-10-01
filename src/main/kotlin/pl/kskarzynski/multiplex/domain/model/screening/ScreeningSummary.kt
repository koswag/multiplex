package pl.kskarzynski.multiplex.domain.model.screening

import java.time.LocalDateTime
import pl.kskarzynski.multiplex.shared.movie.MovieTitle
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

data class ScreeningSummary(
    val id: ScreeningId,
    val title: MovieTitle,
    val startTime: LocalDateTime,
)
