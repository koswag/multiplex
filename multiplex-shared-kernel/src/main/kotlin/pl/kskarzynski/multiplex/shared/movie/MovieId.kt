package pl.kskarzynski.multiplex.shared.movie

import java.util.UUID

@JvmInline
value class MovieId(val value: UUID) {

    override fun toString() = value.toString()

    companion object {
        fun generate() = MovieId(UUID.randomUUID())
    }
}
