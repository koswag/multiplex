package pl.kskarzynski.multiplex.integration

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder

fun ApplicationTestBuilder.configureClient() =
    createClient {
        install(ContentNegotiation) { json() }
    }
