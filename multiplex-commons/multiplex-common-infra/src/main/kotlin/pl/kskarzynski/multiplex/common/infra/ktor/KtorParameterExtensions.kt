package pl.kskarzynski.multiplex.common.infra.ktor

import io.ktor.http.Parameters
import io.ktor.server.plugins.BadRequestException
import pl.kskarzynski.multiplex.shared.misc.PagingRequest

fun Parameters.getPagingRequest(defaultPageSize: Int): PagingRequest =
    PagingRequest(
        pageNumber = getInt("page") ?: 1,
        pageSize = getInt("size") ?: defaultPageSize,
    )

fun Parameters.getInt(name: String): Int? =
    get(name)?.transformOrBadRequest(
        transform = { it.toInt() },
        errorMessage = { "Parameter $name is not a valid int: '$it'" },
    )

private fun <T> String.transformOrBadRequest(
    transform: (String) -> T,
    errorMessage: (String) -> String,
): T =
    runCatching { transform(this) }
        .getOrElse { exc ->
            throw BadRequestException(errorMessage(this), exc)
        }
