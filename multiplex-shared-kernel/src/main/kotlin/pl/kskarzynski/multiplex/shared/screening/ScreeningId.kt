package pl.kskarzynski.multiplex.shared.screening

import java.util.UUID

@JvmInline
value class ScreeningId(val value: UUID) {

    override fun toString() = value.toString()

    companion object {
        fun generate() = ScreeningId(UUID.randomUUID())
    }
}
