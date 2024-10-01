package pl.kskarzynski.multiplex.utils.datetime

import java.time.Clock
import java.time.LocalDateTime

fun Clock.currentTime(): LocalDateTime = LocalDateTime.now(this)
