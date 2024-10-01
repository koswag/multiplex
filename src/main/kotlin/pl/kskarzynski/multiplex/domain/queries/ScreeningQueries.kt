package pl.kskarzynski.multiplex.domain.queries

import java.time.LocalDateTime
import pl.kskarzynski.multiplex.domain.model.screening.Screening
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningSummary
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

interface ScreeningQueries {
    suspend fun findScreeningsByTime(time: LocalDateTime): List<ScreeningSummary>
    suspend fun findScreening(id: ScreeningId): Screening?
}
