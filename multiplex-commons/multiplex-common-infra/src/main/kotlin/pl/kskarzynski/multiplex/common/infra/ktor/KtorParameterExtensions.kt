package pl.kskarzynski.multiplex.common.infra.ktor

import io.ktor.http.Parameters
import io.ktor.server.plugins.BadRequestException
import java.util.UUID
import pl.kskarzynski.multiplex.shared.movie.MovieId

fun <T> Parameters.getRequired(name: String, transform: (String) -> T): T =
    get(name)?.let(transform)
        ?: throw BadRequestException("Parameter $name is required")

fun Parameters.getRequiredUuid(name: String): UUID =
    getRequired(name) { value ->
        runCatching { UUID.fromString(value) }
            .getOrElse { exc ->
                throw BadRequestException("Parameter $name is not a valid UUID: '$value'", exc)
            }
    }

fun Parameters.getRequiredMovieId(name: String): MovieId =
    getRequiredUuid(name)
        .let(::MovieId)
