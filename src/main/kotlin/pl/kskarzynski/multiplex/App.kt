package pl.kskarzynski.multiplex

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import org.koin.core.context.startKoin
import pl.kskarzynski.multiplex.movies.service.config.MovieModule
import pl.kskarzynski.multiplex.movies.service.rest.moviesModule
import pl.kskarzynski.multiplex.rooms.infra.config.RoomModule
import pl.kskarzynski.multiplex.screenings.infra.config.ScreeningModule

fun main() {
    startKoin {
        modules(
            MovieModule,
            RoomModule,
            ScreeningModule,
        )
    }

    embeddedServer(Netty, port = 8080) {
        configureApplication()
        moviesModule()
    }.start(wait = true)
}

fun Application.configureApplication() {
    install(ContentNegotiation) {
        json()
    }

    install(Resources)
}
