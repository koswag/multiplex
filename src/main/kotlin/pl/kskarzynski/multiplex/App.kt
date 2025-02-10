package pl.kskarzynski.multiplex

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import java.time.Clock
import org.koin.core.context.startKoin
import org.koin.dsl.module
import pl.kskarzynski.multiplex.auth.AuthenticationModule.authModule
import pl.kskarzynski.multiplex.movies.service.config.MovieModule
import pl.kskarzynski.multiplex.movies.service.rest.movieModule
import pl.kskarzynski.multiplex.rooms.service.config.RoomModule
import pl.kskarzynski.multiplex.rooms.service.rest.RoomRestModule.roomModule
import pl.kskarzynski.multiplex.screenings.infra.config.ScreeningModule
import pl.kskarzynski.multiplex.screenings.infra.rest.ScreeningRestModule.screeningModule

val CommonModule = module {
    single<Clock> { Clock.systemDefaultZone() }
}

fun main() {
    startKoin {
        modules(
            CommonModule,
            MovieModule,
            RoomModule,
            ScreeningModule,
        )
    }

    Jobs.startAll()

    embeddedServer(Netty, port = 8080) {
        configureApplication()

        authModule()
        movieModule()
        roomModule()
        screeningModule()
    }.start(wait = true)
}

fun Application.configureApplication() {
    install(ContentNegotiation) {
        json()
    }

    install(Resources)
}
