package pl.kskarzynski.multiplex.integration

import io.ktor.server.testing.TestApplicationBuilder
import pl.kskarzynski.multiplex.configureApplication
import pl.kskarzynski.multiplex.movies.service.rest.MoviesRestModule.moviesModule

fun TestApplicationBuilder.multiplexApplication() {
    application {
        configureApplication()
        moviesModule()
    }
}
