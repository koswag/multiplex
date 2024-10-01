package pl.kskarzynski.multiplex.domain.model.screening

import java.util.UUID

@JvmInline
value class MovieId(val value: UUID) {
    companion object {
        fun generate() = MovieId(UUID.randomUUID())
    }
}
