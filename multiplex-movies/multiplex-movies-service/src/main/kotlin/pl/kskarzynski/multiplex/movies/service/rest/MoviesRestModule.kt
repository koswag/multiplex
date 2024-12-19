@file:UseSerializers(UuidSerializer::class)

package pl.kskarzynski.multiplex.movies.service.rest

import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import java.util.UUID
import kotlinx.serialization.UseSerializers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.common.infra.ktor.notFound
import pl.kskarzynski.multiplex.movies.service.adapter.data.MovieRepository
import pl.kskarzynski.multiplex.movies.service.rest.dto.MovieDto
import pl.kskarzynski.multiplex.movies.service.rest.dto.MoviePatchDto
import pl.kskarzynski.multiplex.movies.service.rest.dto.applyPatch
import pl.kskarzynski.multiplex.movies.service.rest.dto.toDomain
import pl.kskarzynski.multiplex.movies.service.rest.dto.toDto
import pl.kskarzynski.multiplex.shared.movie.MovieId

@Resource("/api/movies")
private class Movies {

    @Resource("/{id}")
    class Get(val parent: Movies = Movies(), val id: UUID) {
        val movieId get() = MovieId(id)
    }

    @Resource("")
    class Create(val parent: Movies = Movies())

    @Resource("/{id}")
    class Update(val parent: Movies = Movies(), val id: UUID) {
        val movieId get() = MovieId(id)
    }
}

object MoviesRestModule : KoinComponent {
    val movieRepository by inject<MovieRepository>()

    fun Application.moviesModule() {
        routing {

            get<Movies.Get> { params ->
                val movie = movieRepository.findById(params.movieId)

                if (movie != null) {
                    call.respond(movie.toDto())
                } else {
                    call.respond(NotFound)
                }
            }

            post<Movies.Create> {
                val movie = call.receive<MovieDto>().toDomain()
                movieRepository.save(movie)

                call.respond(Created, movie.toDto())
            }

            patch<Movies.Update> { params ->
                val patch = call.receive<MoviePatchDto>()
                val movie = movieRepository.findById(params.movieId)
                    ?: notFound("Movie of ID '${params.movieId}' not found")

                val updated = movie.applyPatch(patch)
                if (updated != movie) {
                    movieRepository.save(updated)
                }

                call.respond(updated.toDto())
            }
        }
    }
}
