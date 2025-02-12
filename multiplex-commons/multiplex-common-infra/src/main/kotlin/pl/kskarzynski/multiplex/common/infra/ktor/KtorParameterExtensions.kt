package pl.kskarzynski.multiplex.common.infra.ktor

import io.ktor.http.Parameters
import io.ktor.server.plugins.BadRequestException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

fun Parameters.getLocalDateTime(name: String): LocalDateTime? =
    get(name)?.transformOrBadRequest(
        transform = { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) },
        errorMessage = { "Parameter $name is not a valid datetime: '$it'" },
    )

private fun <T> String.transformOrBadRequest(
    transform: (String) -> T,
    errorMessage: (String) -> String,
): T =
    runCatching { transform(this) }
        .getOrElse { exc ->
            throw BadRequestException(errorMessage(this), exc)
        }
