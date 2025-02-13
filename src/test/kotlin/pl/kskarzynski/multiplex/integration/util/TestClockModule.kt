package pl.kskarzynski.multiplex.integration.util

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.koin.dsl.module

private const val TEST_CURRENT_EPOCH_MILLI = 1739484939877

val TestClockModule = module {
    single<Clock> {
        Clock.fixed(
            Instant.ofEpochMilli(TEST_CURRENT_EPOCH_MILLI),
            ZoneId.systemDefault(),
        )
    }
}
