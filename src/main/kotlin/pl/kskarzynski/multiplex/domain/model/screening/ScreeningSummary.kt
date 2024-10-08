package pl.kskarzynski.multiplex.domain.model.screening

import java.time.LocalDateTime
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

data class ScreeningSummary(
    val id: ScreeningId,
    val movieId: MovieId,
    val roomId: RoomId,
    val startTime: LocalDateTime,
)
