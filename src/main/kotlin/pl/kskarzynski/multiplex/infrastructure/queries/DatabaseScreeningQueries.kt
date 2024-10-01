package pl.kskarzynski.multiplex.infrastructure.queries

import java.time.LocalDateTime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.springframework.stereotype.Service
import pl.kskarzynski.multiplex.domain.model.screening.Screening
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningSummary
import pl.kskarzynski.multiplex.domain.queries.ScreeningQueries
import pl.kskarzynski.multiplex.infrastructure.model.ScreeningTable
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

@Service
class DatabaseScreeningQueries : ScreeningQueries {

    override suspend fun findScreeningsByTime(time: LocalDateTime): List<ScreeningSummary> =
        newSuspendedTransaction {
            ScreeningTable.findScreeningsAfter(time)
        }

    override suspend fun findScreening(id: ScreeningId): Screening? =
        newSuspendedTransaction {
            ScreeningTable.findScreening(id)
        }
}
