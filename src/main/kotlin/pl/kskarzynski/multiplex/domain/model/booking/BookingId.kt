package pl.kskarzynski.multiplex.domain.model.booking

import java.util.UUID

@JvmInline
value class BookingId(val value: UUID) {
    companion object {
        fun generate() = BookingId(UUID.randomUUID())
    }
}
