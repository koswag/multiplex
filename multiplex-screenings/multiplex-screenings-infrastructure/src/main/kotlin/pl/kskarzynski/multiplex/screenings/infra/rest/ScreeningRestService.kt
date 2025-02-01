package pl.kskarzynski.multiplex.screenings.infra.rest

import arrow.core.EitherNel
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.raise.zipOrAccumulate
import arrow.core.toEitherNel
import java.time.Clock
import pl.kskarzynski.multiplex.common.utils.datetime.currentTime
import pl.kskarzynski.multiplex.common.utils.datetime.isBefore
import pl.kskarzynski.multiplex.movies.api.service.MovieService
import pl.kskarzynski.multiplex.rooms.api.service.RoomService
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.port.data.ScreeningRepository
import pl.kskarzynski.multiplex.screenings.domain.port.usecase.BookScreeningUseCase
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.CreateScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.PatchScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningListItemDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto.MovieDoesNotExist
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto.PastScreeningTime
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto.RoomDoesNotExist
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.applyPatch
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingIdDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.BookingRequestDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.toDomain
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.booking.toDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.toDto
import pl.kskarzynski.multiplex.shared.misc.Page
import pl.kskarzynski.multiplex.shared.misc.PagingRequest
import pl.kskarzynski.multiplex.shared.misc.map
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

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

    suspend fun getAllByMovie(movieId: MovieId, paging: PagingRequest): Page<ScreeningListItemDto> =
        screeningRepository.findScreeningsByMovie(movieId, paging)
            .map { it.toDto() }

    suspend fun createScreening(dto: CreateScreeningDto): ScreeningValidationResult =
        either {
            val movie = movieService.findMovie(MovieId(dto.movieId))
            val room = roomService.findRoom(RoomId(dto.roomId))

            zipOrAccumulate(
                { ensureNotNull(movie) { MovieDoesNotExist(dto.movieId) } },
                { ensureNotNull(room) { RoomDoesNotExist(dto.roomId) } },
                { ensure(dto.startTime isBefore clock.currentTime()) { PastScreeningTime(dto.startTime) } },
            ) { movie, room, _ ->
                val screening = Screening(
                    id = ScreeningId.generate(),
                    movieId = movie.id,
                    room = room,
                    startTime = ScreeningStartTime(dto.startTime),
                    bookings = emptyList(),
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
                ensure(patch.startTime isBefore clock.currentTime()) { PastScreeningTime(patch.startTime) }
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
            .getOrElse { bookingErrors -> return BookingResult.BookingFailure(bookingErrors.map { it.toDto() }) }

        return BookingResult.Success(BookingIdDto(booking.id.value))
    }

    private suspend fun mapToDtoWithMovie(screening: Screening): ScreeningDto {
        val movie = movieService.findMovie(screening.movieId)
            ?: error("Movie of ID ${screening.movieId} not found (screening ID: ${screening.id})")
        return screening.toDto(movie)
    }
}

private fun EitherNel<ScreeningValidationErrorDto, ScreeningDto>.toScreeningValidationResult(): ScreeningValidationResult =
    fold(
        { errors -> ScreeningValidationResult.Failure(errors) },
        { screening -> ScreeningValidationResult.Success(screening) },
    )
