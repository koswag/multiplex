package pl.kskarzynski.multiplex.common.utils.datetime

import java.time.Clock
import java.time.LocalDateTime
import java.time.chrono.ChronoLocalDateTime
import kotlin.time.Duration

fun Clock.currentTime(): LocalDateTime = LocalDateTime.now(this)

infix fun LocalDateTime.isBefore(other: ChronoLocalDateTime<*>): Boolean = this.isBefore(other)

infix fun LocalDateTime.isAfter(other: ChronoLocalDateTime<*>): Boolean = this.isAfter(other)

operator fun LocalDateTime.plus(duration: Duration): LocalDateTime = this.plusNanos(duration.inWholeNanoseconds)

operator fun LocalDateTime.minus(duration: Duration): LocalDateTime = this.minusNanos(duration.inWholeNanoseconds)
