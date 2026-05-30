package pl.kskarzynski.multiplex.shared.screening

import java.time.DayOfWeek
import java.time.LocalDateTime

@JvmInline
value class ScreeningStartTime(val value: LocalDateTime) {

    val dayOfWeek: DayOfWeek
        get() = value.dayOfWeek

    val hour: Int
        get() = value.hour
}
