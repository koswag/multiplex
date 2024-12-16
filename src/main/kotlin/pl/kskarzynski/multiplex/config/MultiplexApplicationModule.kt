package pl.kskarzynski.multiplex.config

import org.koin.dsl.module
import pl.kskarzynski.multiplex.movies.infra.config.MovieModule
import pl.kskarzynski.multiplex.rooms.infra.config.RoomModule
import pl.kskarzynski.multiplex.screenings.infra.config.ScreeningModule

val MultiplexApplicationModule =
    module {
        includes(
            RoomModule,
            MovieModule,
            ScreeningModule,
        )
    }
