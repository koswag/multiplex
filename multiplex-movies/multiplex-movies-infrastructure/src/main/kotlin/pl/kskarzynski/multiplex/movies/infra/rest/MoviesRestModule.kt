package pl.kskarzynski.multiplex.movies.infra.rest

import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.koin
import pl.kskarzynski.multiplex.common.infra.ktor.getRequiredMovieId
import pl.kskarzynski.multiplex.movies.domain.port.MovieRepository
import pl.kskarzynski.multiplex.movies.infra.config.MovieModule
import pl.kskarzynski.multiplex.movies.infra.rest.dto.MovieDto
import pl.kskarzynski.multiplex.movies.infra.rest.dto.toDomain
import pl.kskarzynski.multiplex.movies.infra.rest.dto.toDto

context(Application)
fun moviesModule() {
    koin {
        modules(MovieModule)
    }

    movieRouting()
}

context(Application)
private fun movieRouting() = routing {
    val movieRepository by inject<MovieRepository>()

    route("/api/movies") {
        get("/{id}") {
            val id = call.parameters.getRequiredMovieId("id")
            val movie = movieRepository.findMovie(id)

            if (movie != null) {
                call.respond(movie.toDto())
            } else {
                call.respond(NotFound)
            }
        }

        post {
            val movie = call.receive<MovieDto>().toDomain()
            movieRepository.saveMovie(movie)
        }
    }
}
