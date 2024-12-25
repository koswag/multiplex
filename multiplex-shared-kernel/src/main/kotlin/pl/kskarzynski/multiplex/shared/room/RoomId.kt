package pl.kskarzynski.multiplex.shared.room

import java.util.UUID

@JvmInline
value class RoomId(val value: UUID) {

    override fun toString() = value.toString()

    companion object {
        fun generate() = RoomId(UUID.randomUUID())
    }
}
