package pl.kskarzynski.multiplex.shared.screening

import java.util.UUID

@JvmInline
value class ScreeningId(val value: UUID) {
    companion object {
        fun generate() = ScreeningId(UUID.randomUUID())
    }
}
