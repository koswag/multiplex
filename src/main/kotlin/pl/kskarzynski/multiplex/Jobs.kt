package pl.kskarzynski.multiplex

import kotlin.time.Duration.Companion.seconds
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.kskarzynski.multiplex.screenings.infra.job.CancelExpiredBookingsJob

object Jobs : KoinComponent {

    private val cancelExpiredBookingsJob by inject<CancelExpiredBookingsJob>()

    fun startAll() {
        cancelExpiredBookingsJob.start(interval = 5.seconds) // TODO: Move interval to configuration
    }
}
