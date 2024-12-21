package pl.kskarzynski.multiplex.common.infra.ktor

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respondText
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

val CONTENT_TYPE_JSON_UTF_8 = ContentType.Application.Json.withCharset(Charsets.UTF_8)

fun badRequest(message: String? = null, cause: Throwable? = null): Nothing =
    throw BadRequestException(message ?: "Bad request", cause)

fun notFound(message: String? = null): Nothing =
    throw NotFoundException(message ?: "Resource not found")

suspend inline fun <reified R> ApplicationCall.respond(status: HttpStatusCode, content: NonEmptyList<R>) {
    val serialized = Json.encodeToString(NonEmptyListSerializer(serializer()), content)
    respondText(serialized, CONTENT_TYPE_JSON_UTF_8, status)
}
