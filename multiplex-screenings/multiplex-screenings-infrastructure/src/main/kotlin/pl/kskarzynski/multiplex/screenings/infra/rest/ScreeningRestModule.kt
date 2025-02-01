@file:UseSerializers(UuidSerializer::class)

package pl.kskarzynski.multiplex.screenings.infra.rest

import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.util.UUID
import kotlinx.serialization.UseSerializers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.common.infra.ktor.getPagingRequest
import pl.kskarzynski.multiplex.common.infra.ktor.respond
import pl.kskarzynski.multiplex.common.infra.misc.toDto
import pl.kskarzynski.multiplex.screenings.infra.rest.ScreeningValidationResult.Failure
import pl.kskarzynski.multiplex.screenings.infra.rest.ScreeningValidationResult.Success
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.CreateScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.PatchScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingErrorDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingRequestDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingValidationErrorDto
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

@Resource("/api/rooms")
private class Screenings {

    @Resource("/{id}")
    class Get(val parent: Screenings, val id: UUID) {
        val screeningId get() = ScreeningId(id)
    }

    @Resource("/{movie}")
    class GetAllByMovie(val parent: Screenings, val movie: UUID) {
        val movieId get() = MovieId(movie)
    }

    @Resource("")
    class Create(val parent: Screenings)

    @Resource("/{id}")
    class Update(val parent: Screenings, val id: UUID) {
        val screeningId get() = ScreeningId(id)
    }

    @Resource("/{id}/book")
    class CreateBooking(val parent: Screenings, val id: UUID) {
        val screeningId get() = ScreeningId(id)
    }
}

object ScreeningRestModule : KoinComponent {

    private val screeningRestService by inject<ScreeningRestService>()

    private const val DEFAULT_PAGE_SIZE = 10

    fun Application.screeningModule() {
        routing {
            get<Screenings.Get> { params ->
                val screening = screeningRestService.getScreening(params.screeningId)

                if (screening != null) {
                    call.respond(screening)
                } else {
                    call.respond(NotFound)
                }
            }

            get<Screenings.GetAllByMovie> { params ->
                val paging = call.request.queryParameters.getPagingRequest(DEFAULT_PAGE_SIZE)
                val screenings = screeningRestService.getAllByMovie(params.movieId, paging)

                call.respond(screenings.toDto())
            }

            post<Screenings.CreateBooking> { params ->
                val dto = call.receive<BookingRequestDto>()
                val bookingResult = screeningRestService.bookScreening(params.screeningId, dto)

                when (bookingResult) {
                    is BookingResult.ScreeningDoesNotExist -> {
                        call.respond(NotFound, "Screening of ID ${bookingResult.screeningId} not found")
                    }
                    is BookingResult.ValidationFailure -> {
                        call.respond<BookingValidationErrorDto>(BadRequest, bookingResult.validationErrors)
                    }
                    is BookingResult.BookingFailure -> {
                        call.respond<BookingErrorDto>(Conflict, bookingResult.bookingErrors)
                    }
                    is BookingResult.Success -> {
                        call.respond(bookingResult.bookingId) // TODO: `Created` or `Ok`?
                    }
                }
            }

            post<Screenings.Create> {
                // TODO: Authentication
                val dto = call.receive<CreateScreeningDto>()
                val creationResult = screeningRestService.createScreening(dto)

                when (creationResult) {
                    is Success -> call.respond(Created, creationResult.screening)
                    is Failure -> call.respond<ScreeningValidationErrorDto>(BadRequest, creationResult.errors)
                }
            }

            patch<Screenings.Update> { params ->
                // TODO: Authentication
                val dto = call.receive<PatchScreeningDto>()
                val updateResult = screeningRestService.updateScreening(params.screeningId, dto)

                when (updateResult) {
                    null -> call.respond(NotFound)
                    is Success -> call.respond(Created, updateResult.screening)
                    is Failure -> call.respond<ScreeningValidationErrorDto>(BadRequest, updateResult.errors)
                }
            }
        }
    }
}
