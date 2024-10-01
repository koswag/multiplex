package pl.kskarzynski.multiplex.shared.movie

import java.util.UUID

@JvmInline
value class MovieId(val value: UUID) {
    companion object {
        fun generate() = MovieId(UUID.randomUUID())
    }
}
