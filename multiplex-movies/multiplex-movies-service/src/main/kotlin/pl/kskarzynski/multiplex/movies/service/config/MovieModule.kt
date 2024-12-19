package pl.kskarzynski.multiplex.movies.service.config

import org.koin.dsl.module
import pl.kskarzynski.multiplex.movies.api.service.MovieService
import pl.kskarzynski.multiplex.movies.service.adapter.api.service.MovieServiceImpl
import pl.kskarzynski.multiplex.movies.service.adapter.data.DatabaseMovieRepository
import pl.kskarzynski.multiplex.movies.service.adapter.data.MovieQueries
import pl.kskarzynski.multiplex.movies.service.adapter.data.MovieRepository

val MovieModule =
    module {
        single<MovieRepository> { DatabaseMovieRepository() }
        single<MovieQueries> { get<MovieRepository>() }
        single<MovieService> { MovieServiceImpl(movieQueries = get()) }
    }
