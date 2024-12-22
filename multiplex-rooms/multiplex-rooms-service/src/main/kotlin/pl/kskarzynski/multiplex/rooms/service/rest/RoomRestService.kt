package pl.kskarzynski.multiplex.rooms.service.rest

import arrow.core.EitherNel
import arrow.core.NonEmptyList
import arrow.core.nel
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.kskarzynski.multiplex.common.utils.arrow.accumulateErrors
import pl.kskarzynski.multiplex.rooms.service.data.RoomRepository
import pl.kskarzynski.multiplex.rooms.service.rest.dto.CreateRoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.PatchRoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.RoomNumberAlreadyExists
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.SeatValidationError
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.SeatValidationError.InvalidSeatNumber
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.SeatValidationError.InvalidSeatRow
import pl.kskarzynski.multiplex.rooms.service.rest.dto.SeatDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.applyPatch
import pl.kskarzynski.multiplex.rooms.service.rest.dto.toDomain
import pl.kskarzynski.multiplex.rooms.service.rest.dto.toDto
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

object RoomRestService : KoinComponent {

    private val roomRepository by inject<RoomRepository>()

    suspend fun getRoom(id: RoomId): RoomDto? =
        roomRepository.findById(id)
            ?.toDto()

    suspend fun createRoom(dto: CreateRoomDto): RoomValidationResult =
        either {
            ensureUniqueNumber(dto.number)
            ensureValidSeats(dto.seats)

            val room = dto.toDomain()
            roomRepository.save(room)

            room.toDto()
        }.toRoomValidationResult()

    suspend fun updateRoom(id: RoomId, patch: PatchRoomDto): RoomValidationResult? {
        val existentRoom = roomRepository.findById(id)
            ?: return null

        return either {
            if (patch.number != null && patch.number != existentRoom.number.value) {
                ensureUniqueNumber(patch.number)
            }

            if (patch.seats != null) {
                ensureValidSeats(patch.seats)
            }

            val updatedRoom = existentRoom.applyPatch(patch)
            if (updatedRoom != existentRoom) {
                roomRepository.save(updatedRoom)
            }

            updatedRoom.toDto()
        }.toRoomValidationResult()
    }

    context(Raise<NonEmptyList<RoomNumberAlreadyExists>>)
    private suspend fun ensureUniqueNumber(roomNumber: Int) {
        val existentRoom = roomRepository.findByNumber(RoomNumber(roomNumber))
        ensure(existentRoom == null) { RoomNumberAlreadyExists(roomNumber).nel() }
    }

    context(Raise<NonEmptyList<SeatValidationError>>)
    private fun ensureValidSeats(seats: NonEmptyList<SeatDto>) {
        accumulateErrors(seats) { seat ->
            accumulateErrors(
                { ensure(seat.row >= SeatRow.MIN_VALUE) { InvalidSeatRow(seat.row, seat.number) } },
                { ensure(seat.number >= SeatNumber.MIN_VALUE) { InvalidSeatNumber(seat.number, seat.number) } },
            )
        }
    }
}

private fun EitherNel<RoomValidationError, RoomDto>.toRoomValidationResult(): RoomValidationResult =
    fold(
        { errors -> RoomValidationResult.Failure(errors) },
        { room -> RoomValidationResult.Success(room) },
    )
