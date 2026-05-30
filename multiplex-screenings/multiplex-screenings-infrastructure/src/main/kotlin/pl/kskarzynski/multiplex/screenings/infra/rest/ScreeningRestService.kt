@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.screenings.infra.rest

import arrow.core.EitherNel
import arrow.core.getOrElse
import arrow.core.nel
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.raise.zipOrAccumulate
import arrow.core.toEitherNel
import io.ktor.server.plugins.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.kskarzynski.multiplex.common.infra.misc.toDto
import pl.kskarzynski.multiplex.common.utils.datetime.currentTime
import pl.kskarzynski.multiplex.common.utils.datetime.isAfter
import pl.kskarzynski.multiplex.movies.api.service.MovieService
import pl.kskarzynski.multiplex.rooms.api.service.RoomService
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.model.request.MovieScreeningSearchRequest
import pl.kskarzynski.multiplex.screenings.domain.port.data.ScreeningRepository
import pl.kskarzynski.multiplex.screenings.domain.port.usecase.BookScreeningUseCase
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.*
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto.*
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingRequestDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.toDomain
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.toDto
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.misc.map
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime
import java.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi

private val SCREENING_ROOM_FLOW_INTERVAL = 3000L.milliseconds

class ScreeningRestService(
    private val screeningRepository: ScreeningRepository,
    private val bookScreeningUseCase: BookScreeningUseCase,
    private val movieService: MovieService,
    private val roomService: RoomService,
    private val clock: Clock,
) {
    suspend fun getScreening(id: ScreeningId): ScreeningDto? =
        screeningRepository.findScreening(id)
            ?.let { mapToDtoWithMovie(it) }

    suspend fun getAllByMovie(request: MovieScreeningSearchRequest): MovieScreeningSearchResult {
        movieService.findMovie(request.movieId)
            ?: return MovieScreeningSearchResult.MovieNotFound

        val screenings = screeningRepository.findScreeningsByMovie(request)
            .map { it.toDto() }

        return MovieScreeningSearchResult.Success(screenings.toDto())
    }

    suspend fun createScreening(dto: CreateScreeningDto): ScreeningValidationResult =
        either {
            val movie = movieService.findMovie(MovieId(dto.movieId))
            val room = roomService.findRoom(RoomId(dto.roomId))

            zipOrAccumulate(
                { ensureNotNull(movie) { MovieDoesNotExist(dto.movieId) } },
                { ensureNotNull(room) { RoomDoesNotExist(dto.roomId) } },
                { ensure(dto.startTime isAfter clock.currentTime()) { PastScreeningTime(dto.startTime) } },
            ) { movie, room, _ ->
                val screening = Screening(
                    movieId = movie.id,
                    room = room,
                    startTime = ScreeningStartTime(dto.startTime),
                )
                screeningRepository.save(screening)

                screening.toDto(movie)
            }
        }.toScreeningValidationResult()

    suspend fun updateScreening(screeningId: ScreeningId, patch: PatchScreeningDto): ScreeningValidationResult? {
        val screening = screeningRepository.findScreening(screeningId)
            ?: return null

        return either {
            if (patch.startTime != null) {
                ensure(patch.startTime isAfter clock.currentTime()) { PastScreeningTime(patch.startTime) }
            }

            val updatedScreening = screening.applyPatch(patch)
            if (updatedScreening != screening) {
                screeningRepository.save(updatedScreening)
            }

            mapToDtoWithMovie(updatedScreening)
        }.toEitherNel().toScreeningValidationResult()
    }

    suspend fun bookScreening(screeningId: ScreeningId, dto: BookingRequestDto): BookingResult {
        val screening = screeningRepository.findScreening(screeningId)
            ?: return BookingResult.ScreeningDoesNotExist(screeningId)

        val bookingRequest = dto.toDomain(screeningId, clock.currentTime())
            .getOrElse { validationErrors -> return BookingResult.ValidationFailure(validationErrors) }

        val booking = bookScreeningUseCase.execute(screening, bookingRequest)
            .getOrElse { bookingErrors -> return BookingResult.BookingFailure(bookingErrors) }

        return BookingResult.Success(booking.id)
    }

    private suspend fun mapToDtoWithMovie(screening: Screening): ScreeningDto {
        val movie = movieService.findMovie(screening.movieId)
            ?: error("Movie of ID ${screening.movieId} not found (screening ID: ${screening.id})")
        return screening.toDto(movie)
    }

    suspend fun confirmBooking(screeningId: ScreeningId, bookingId: BookingId): BookingConfirmationResult {
        val screening = screeningRepository.findScreening(screeningId)
            ?: return BookingConfirmationResult.ScreeningDoesNotExist(screeningId)

        val updatedScreening = screening.confirmBooking(bookingId, clock.currentTime())
            .getOrElse { error ->
                return BookingConfirmationResult.ConfirmationFailure(error.toDto(screeningId, bookingId).nel())
            }

        screeningRepository.save(updatedScreening)
        return BookingConfirmationResult.Success(bookingId.toDto())
    }

    fun screeningRoomState(screeningId: ScreeningId): Flow<ScreeningRoomDto> =
        flow {
            while (true) {
                val screening = screeningRepository.findScreening(screeningId)
                    ?: throw NotFoundException("Screening of ID $screeningId not found")
                emit(screening.toScreeningRoomDto())
                delay(SCREENING_ROOM_FLOW_INTERVAL)
            }
        }
}

private fun EitherNel<ScreeningValidationErrorDto, ScreeningDto>.toScreeningValidationResult(): ScreeningValidationResult =
    fold(
        { errors -> ScreeningValidationResult.Failure(errors) },
        { screening -> ScreeningValidationResult.Success(screening) },
    )
