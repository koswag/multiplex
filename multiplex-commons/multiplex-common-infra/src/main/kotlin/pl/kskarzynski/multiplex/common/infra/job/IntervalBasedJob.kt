package pl.kskarzynski.multiplex.common.infra.job

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging

abstract class IntervalBasedJob(val name: String) {

    private val logger = KotlinLogging.logger(name)
    private val scope = CoroutineScope(SupervisorJob())

    abstract suspend fun run()

    open suspend fun handle(exc: Exception) {
        logger.error(exc) { "$name job failed" }
    }

    fun start(
        interval: Duration,
        context: CoroutineContext = EmptyCoroutineContext,
    ) {
        scope.launch(context) {
            while (true) {
                try {
                    delay(interval)
                    run()
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (exc: Exception) {
                    handle(exc)
                }
            }
        }
    }

    fun cancel() {
        scope.cancel()
    }
}
