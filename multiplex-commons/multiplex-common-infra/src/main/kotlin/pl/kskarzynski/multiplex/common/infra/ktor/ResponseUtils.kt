package pl.kskarzynski.multiplex.common.infra.ktor

import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException

fun badRequest(message: String, cause: Throwable? = null): Nothing =
    throw BadRequestException(message, cause)

fun notFound(message: String): Nothing =
    throw NotFoundException(message)
