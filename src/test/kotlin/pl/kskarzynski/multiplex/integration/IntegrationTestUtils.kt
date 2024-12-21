package pl.kskarzynski.multiplex.integration

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.TestApplicationBuilder
import pl.kskarzynski.multiplex.configureApplication
import pl.kskarzynski.multiplex.movies.service.rest.MoviesRestModule.moviesModule

fun TestApplicationBuilder.multiplexApplication() {
    application {
        configureApplication()
        moviesModule()
    }
}

fun ApplicationTestBuilder.clientWithJson() =
    createClient {
        install(ContentNegotiation) { json() }
    }
