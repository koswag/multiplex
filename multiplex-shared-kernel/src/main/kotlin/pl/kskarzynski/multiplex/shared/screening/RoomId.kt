package pl.kskarzynski.multiplex.shared.screening

import java.util.UUID

@JvmInline
value class RoomId(val value: UUID) {
    companion object {
        fun generate() = RoomId(UUID.randomUUID())
    }
}
