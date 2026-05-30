@file:UseSerializers(ScreeningIdSerializer::class, MovieIdSerializer::class, BookingIdSerializer::class)

package pl.kskarzynski.multiplex.screenings.infra.rest

import io.ktor.http.*
import io.ktor.http.CacheControl.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.resources.*
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.take
import kotlinx.serialization.UseSerializers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.kskarzynski.multiplex.common.infra.io.writeJson
import pl.kskarzynski.multiplex.common.infra.json.serializer.BookingIdSerializer
import pl.kskarzynski.multiplex.common.infra.json.serializer.MovieIdSerializer
import pl.kskarzynski.multiplex.common.infra.json.serializer.ScreeningIdSerializer
import pl.kskarzynski.multiplex.common.infra.ktor.getLocalDateTime
import pl.kskarzynski.multiplex.common.infra.ktor.getPagingRequest
import pl.kskarzynski.multiplex.common.infra.ktor.respond
import pl.kskarzynski.multiplex.screenings.domain.model.request.MovieScreeningSearchRequest
import pl.kskarzynski.multiplex.screenings.infra.rest.ScreeningValidationResult.Failure
import pl.kskarzynski.multiplex.screenings.infra.rest.ScreeningValidationResult.Success
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.CreateScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.PatchScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingConfirmationErrorDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingRequestDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.toDto
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

private const val DEFAULT_PAGE_SIZE = 10

@Resource("/api")
private class Screenings {

    @Resource("/screenings/{screeningId}")
    class Get(val parent: Screenings, val screeningId: ScreeningId)

    @Resource("/movies/{movieId}/screenings")
    class GetAllByMovie(val parent: Screenings, val movieId: MovieId)

    @Resource("/screenings")
    class Create(val parent: Screenings)

    @Resource("/screenings/{screeningId}")
    class Update(val parent: Screenings, val screeningId: ScreeningId)

    @Resource("/screenings/{screeningId}/bookings")
    class CreateBooking(val parent: Screenings, val screeningId: ScreeningId)

    @Resource("/screenings/{screeningId}/bookings/{bookingId}/confirm")
    class ConfirmBooking(val parent: Screenings, val screeningId: ScreeningId, val bookingId: BookingId)
}

@Resource("/sse")
private class ScreeningsSse {

    @Resource("/screenings/{screeningId}/room")
    class ScreeningRoomStateStream(val parent: ScreeningsSse, val screeningId: ScreeningId)
}

object ScreeningRestModule : KoinComponent {

    private val screeningRestService by inject<ScreeningRestService>()

    fun Application.screeningModule() {
        routing {
            get<Screenings.Get> { params ->
                val screening = screeningRestService.getScreening(params.screeningId)

                if (screening != null) {
                    call.respond(screening)
                } else {
                    call.respond(NotFound, "Screening of ID ${params.screeningId} not found")
                }
            }

            get<Screenings.GetAllByMovie> { params ->
                val request = call.getMovieScreeningSearchRequest(params.movieId)

                when (
                    val result = screeningRestService.getAllByMovie(request)
                ) {
                    is MovieScreeningSearchResult.MovieNotFound ->
                        call.respond(NotFound, "Movie of ID ${params.movieId} not found")
                    is MovieScreeningSearchResult.Success ->
                        call.respond(result.screenings)
                }
            }

            post<Screenings.CreateBooking> { params ->
                val dto = call.receive<BookingRequestDto>()

                when (
                    val bookingResult = screeningRestService.bookScreening(params.screeningId, dto)
                ) {
                    is BookingResult.Success ->
                        call.respond(bookingResult.bookingId.toDto())
                    is BookingResult.ScreeningDoesNotExist ->
                        call.respond(NotFound, "Screening of ID ${bookingResult.screeningId} not found")
                    is BookingResult.ValidationFailure ->
                        call.respond(BadRequest, bookingResult.validationErrors)
                    is BookingResult.BookingFailure ->
                        call.respond(Conflict, bookingResult.bookingErrors.map { it.toDto() })
                }
            }

            post<Screenings.ConfirmBooking> { params ->
                when (
                    val confirmationResult = screeningRestService.confirmBooking(params.screeningId, params.bookingId)
                ) {
                    is BookingConfirmationResult.ScreeningDoesNotExist ->
                        call.respond(NotFound, "Screening of ID ${confirmationResult.screeningId} not found")
                    is BookingConfirmationResult.Success ->
                        call.respond(confirmationResult.bookingId)
                    is BookingConfirmationResult.ConfirmationFailure ->
                        call.respond<BookingConfirmationErrorDto>(BadRequest, confirmationResult.errors)
                }
            }

            post<Screenings.Create> {
                val dto = call.receive<CreateScreeningDto>()

                when (
                    val creationResult = screeningRestService.createScreening(dto)
                ) {
                    is Success -> call.respond(Created, creationResult.screening)
                    is Failure -> call.respond<ScreeningValidationErrorDto>(BadRequest, creationResult.errors)
                }
            }

            patch<Screenings.Update> { params ->
                val dto = call.receive<PatchScreeningDto>()

                when (
                    val updateResult = screeningRestService.updateScreening(params.screeningId, dto)
                ) {
                    null -> call.respond(NotFound)
                    is Success -> call.respond(updateResult.screening)
                    is Failure -> call.respond<ScreeningValidationErrorDto>(BadRequest, updateResult.errors)
                }
            }

            get<ScreeningsSse.ScreeningRoomStateStream> { params ->
                call.response.cacheControl(NoCache(null))
                call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                    screeningRestService.screeningRoomState(params.screeningId)
                        .take(5) // FIXME: Temporary solution before tests can handle infinite event streams
                        .collect { screeningRoomState ->
                            writeJson(screeningRoomState)
                        }
                }
            }
        }
    }
}

private fun ApplicationCall.getMovieScreeningSearchRequest(movieId: MovieId) =
    MovieScreeningSearchRequest(
        movieId = movieId,
        pagingRequest = request.queryParameters.getPagingRequest(DEFAULT_PAGE_SIZE),
        minStartTime = request.queryParameters.getLocalDateTime("from")?.let(::ScreeningStartTime),
        maxStartTime = request.queryParameters.getLocalDateTime("to")?.let(::ScreeningStartTime),
    )
