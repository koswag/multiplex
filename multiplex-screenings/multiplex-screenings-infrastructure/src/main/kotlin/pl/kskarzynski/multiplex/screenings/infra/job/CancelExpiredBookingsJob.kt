package pl.kskarzynski.multiplex.screenings.infra.job

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import pl.kskarzynski.multiplex.common.infra.job.IntervalBasedJob
import pl.kskarzynski.multiplex.common.utils.datetime.currentTime
import pl.kskarzynski.multiplex.screenings.domain.port.data.ScreeningRepository
import java.time.Clock

// TODO: Tests
class CancelExpiredBookingsJob(
    private val screeningRepository: ScreeningRepository,
    private val clock: Clock,
) : IntervalBasedJob(name = "CancelExpiredBookingsJob") {

    override suspend fun run() {
        coroutineScope {
            val screeningsWithExpiredBookings = screeningRepository.findScreeningsWithExpiredBookings()

            val updatedScreenings = screeningsWithExpiredBookings
                .map { async { it.cancelExpiredBookings(clock.currentTime()) } }
                .awaitAll()

            // TODO: Retries?
            suspendTransaction {
                for (screening in updatedScreenings) {
                    screeningRepository.save(screening)
                }
            }
        }
    }
}
