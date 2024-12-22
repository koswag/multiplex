@file:UseSerializers(UuidSerializer::class)

package pl.kskarzynski.multiplex.movies.service.rest

import io.ktor.http.HttpStatusCode.Companion.BadRequest
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
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.common.infra.ktor.respond
import pl.kskarzynski.multiplex.movies.service.rest.MovieDataValidationResult.Failure
import pl.kskarzynski.multiplex.movies.service.rest.MovieDataValidationResult.Success
import pl.kskarzynski.multiplex.movies.service.rest.dto.CreateMovieDto
import pl.kskarzynski.multiplex.movies.service.rest.dto.PatchMovieDto
import pl.kskarzynski.multiplex.shared.movie.MovieId

@Resource("/api/movies")
private class Movies {

    @Resource("/{id}")
    class Get(val parent: Movies, val id: UUID) {
        val movieId get() = MovieId(id)
    }

    @Resource("")
    class Create(val parent: Movies)

    @Resource("/{id}")
    class Update(val parent: Movies, val id: UUID) {
        val movieId get() = MovieId(id)
    }
}

fun Application.movieModule() {
    routing {
        get<Movies.Get> { params ->
            val movie = MovieRestService.getMovie(params.movieId)

            if (movie != null) {
                call.respond(movie)
            } else {
                call.respond(NotFound)
            }
        }

        post<Movies.Create> {
            val dto = call.receive<CreateMovieDto>()
            val creationResult = MovieRestService.createMovie(dto)

            when (creationResult) {
                is Success -> call.respond(Created, creationResult.movie)
                is Failure -> call.respond(BadRequest, creationResult.errors)
            }
        }

        patch<Movies.Update> { params ->
            val dto = call.receive<PatchMovieDto>()
            val updateResult = MovieRestService.patchMovie(params.movieId, dto)

            when (updateResult) {
                null -> call.respond(NotFound)
                is Success -> call.respond(updateResult.movie)
                is Failure -> call.respond(BadRequest, updateResult.errors)
            }
        }
    }
}
