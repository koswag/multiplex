package pl.kskarzynski.multiplex.common.util.extensions

import java.time.Clock
import java.time.LocalDateTime

fun Clock.currentTime(): LocalDateTime = LocalDateTime.now(this)
