@file:UseSerializers(MovieIdSerializer::class)

package pl.kskarzynski.multiplex.movies.service.rest

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.resources.*
import io.ktor.server.application.Application
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.routing
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.MovieIdSerializer
import pl.kskarzynski.multiplex.common.infra.ktor.respond
import pl.kskarzynski.multiplex.movies.service.rest.MovieValidationResult.Failure
import pl.kskarzynski.multiplex.movies.service.rest.MovieValidationResult.Success
import pl.kskarzynski.multiplex.movies.service.rest.dto.CreateMovieDto
import pl.kskarzynski.multiplex.movies.service.rest.dto.PatchMovieDto
import pl.kskarzynski.multiplex.shared.movie.MovieId

@Resource("/api/movies")
private class Movies {

    @Resource("/{movieId}")
    class Get(val parent: Movies, val movieId: MovieId)

    @Resource("")
    class Create(val parent: Movies)

    @Resource("/{movieId}")
    class Update(val parent: Movies, val movieId: MovieId)
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
            when (
                val creationResult = MovieRestService.createMovie(dto)
            ) {
                is Success -> call.respond(Created, creationResult.movie)
                is Failure -> call.respond(BadRequest, creationResult.errors)
            }
        }

        patch<Movies.Update> { params ->
            val dto = call.receive<PatchMovieDto>()
            when (
                val updateResult = MovieRestService.patchMovie(params.movieId, dto)
            ) {
                null -> call.respond(NotFound)
                is Success -> call.respond(updateResult.movie)
                is Failure -> call.respond(BadRequest, updateResult.errors)
            }
        }
    }
}
