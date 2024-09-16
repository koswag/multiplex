package pl.kskarzynski.multiplex.infrastructure.queries

import java.time.LocalDateTime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Service
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningId
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningInfo
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningSummary
import pl.kskarzynski.multiplex.domain.queries.ScreeningQueries
import pl.kskarzynski.multiplex.infrastructure.model.ScreeningTable

@Service
class DatabaseScreeningQueries : ScreeningQueries {

    override suspend fun findScreeningsByTime(time: LocalDateTime): List<ScreeningSummary> =
        newSuspendedTransaction {
            ScreeningTable.findScreeningsAfter(time)
        }

    override suspend fun findScreeningInfo(id: ScreeningId): ScreeningInfo? =
        newSuspendedTransaction {
            ScreeningTable.findScreeningInfo(id)
        }
}
