package pl.kskarzynski.multiplex.movies.infra.config

import org.koin.dsl.module
import pl.kskarzynski.multiplex.movies.api.service.MovieService
import pl.kskarzynski.multiplex.movies.domain.port.MovieRepository
import pl.kskarzynski.multiplex.movies.infra.adapter.api.service.MovieServiceImpl
import pl.kskarzynski.multiplex.movies.infra.adapter.data.DatabaseMovieRepository

val MovieModule =
    module {
        single<MovieRepository> { DatabaseMovieRepository() }
        single<MovieService> { MovieServiceImpl(movieRepository = get()) }
    }
