package pl.kskarzynski.multiplex.domain.queries

import pl.kskarzynski.multiplex.domain.model.screening.ScreeningId
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningInfo
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningListItem
import java.time.LocalDateTime

interface ScreeningQueries {
    fun findScreeningsByTimeSortedByTitleAndTime(time: LocalDateTime): Collection<ScreeningListItem>
    fun findScreeningInfo(id: ScreeningId): ScreeningInfo?
}
