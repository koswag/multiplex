package pl.kskarzynski.multiplex.domain.queries

import java.time.LocalDateTime
import pl.kskarzynski.multiplex.domain.model.screening.Screening
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningId
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningSummary

interface ScreeningQueries {
    suspend fun findScreeningsByTime(time: LocalDateTime): List<ScreeningSummary>
    suspend fun findScreening(id: ScreeningId): Screening?
}
