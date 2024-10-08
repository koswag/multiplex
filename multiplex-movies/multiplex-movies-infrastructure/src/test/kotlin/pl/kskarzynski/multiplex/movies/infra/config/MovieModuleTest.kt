package pl.kskarzynski.multiplex.movies.infra.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.koin.KoinExtension
import org.koin.test.KoinTest
import org.koin.test.inject
import pl.kskarzynski.multiplex.movies.api.service.MovieService
import pl.kskarzynski.multiplex.movies.domain.port.MovieRepository
import strikt.api.expectThat
import strikt.assertions.isNotNull

class MovieModuleTest : FunSpec(), KoinTest {

    override fun extensions() = listOf(KoinExtension(MovieModule))

    private val movieRepository by inject<MovieRepository>()
    private val movieService by inject<MovieService>()

    init {
        test("Movie component injection") {
            expectThat(movieRepository).isNotNull()
            expectThat(movieService).isNotNull()
        }
    }
}
