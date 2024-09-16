package pl.kskarzynski.multiplex.domain.queries

import java.time.LocalDateTime
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningId
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningInfo
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningSummary

interface ScreeningQueries {
    suspend fun findScreeningsByTime(time: LocalDateTime): List<ScreeningSummary>
    suspend fun findScreeningInfo(id: ScreeningId): ScreeningInfo?
}
