package pl.kskarzynski.multiplex.shared.booking

import java.util.UUID

@JvmInline
value class BookingId(val value: UUID) {
    companion object {
        fun generate() = BookingId(UUID.randomUUID())
    }
}
