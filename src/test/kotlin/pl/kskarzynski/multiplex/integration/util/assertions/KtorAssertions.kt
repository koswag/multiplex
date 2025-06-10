package pl.kskarzynski.multiplex.integration.util.assertions

import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pl.kskarzynski.multiplex.common.infra.ktor.CONTENT_TYPE_JSON_UTF_8
import strikt.api.Assertion

fun Assertion.Builder<HttpResponse>.hasContentTypeJsonUtf8(): Assertion.Builder<HttpResponse> =
    hasContentType(CONTENT_TYPE_JSON_UTF_8)

fun Assertion.Builder<HttpResponse>.hasContentType(expectedContentType: ContentType): Assertion.Builder<HttpResponse> =
    assert("has Content-Type: $expectedContentType") {
        when (val actualContentType = it.contentType()) {
            expectedContentType -> pass()
            null -> fail("Content-Type header is missing")
            else -> fail("Content-Type is $actualContentType")
        }
    }
