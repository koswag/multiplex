package pl.kskarzynski.multiplex.rooms.service.rest

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.koin.core.component.KoinComponent
import pl.kskarzynski.multiplex.common.utils.arrow.accumulateErrors
import pl.kskarzynski.multiplex.common.utils.arrow.flattenErrors
import pl.kskarzynski.multiplex.rooms.service.data.RoomRepository
import pl.kskarzynski.multiplex.rooms.service.rest.dto.CreateRoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.PatchRoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.DuplicatedSeat
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.InvalidRoomNumber
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.InvalidSeatNumber
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.InvalidSeatRow
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.MissingRow
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.MissingSeat
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError.RoomNumberAlreadyExists
import pl.kskarzynski.multiplex.rooms.service.rest.dto.SeatDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.applyPatch
import pl.kskarzynski.multiplex.rooms.service.rest.dto.toDomain
import pl.kskarzynski.multiplex.rooms.service.rest.dto.toDto
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

class RoomRestService(
    private val roomRepository: RoomRepository,
) : KoinComponent {

    suspend fun getRoom(id: RoomId): RoomDto? =
        roomRepository.findById(id)
            ?.toDto()

    suspend fun createRoom(dto: CreateRoomDto): RoomValidationResult =
        either {
            val existentRoomWithNumber = roomRepository.findByNumber(RoomNumber(dto.number))
            accumulateErrors(
                { ensureValidNumber(dto.number, existentRoomWithNumber) },
                { ensureValidSeats(dto.seats) },
            )

            val room = dto.toDomain()
            roomRepository.save(room)

            room.toDto()
        }.flattenErrors().toRoomValidationResult()

    suspend fun updateRoom(id: RoomId, patch: PatchRoomDto): RoomValidationResult? {
        val existentRoom = roomRepository.findById(id)
            ?: return null

        return either {
            val otherRoomWithSameNumber = findOtherRoomWithSameNumber(existentRoom, patch)
            accumulateErrors(
                { if (patch.number != null) ensureValidNumber(patch.number, otherRoomWithSameNumber) },
                { if (patch.seats != null) ensureValidSeats(patch.seats) },
            )

            val updatedRoom = existentRoom.applyPatch(patch)
            if (updatedRoom != existentRoom) {
                roomRepository.save(updatedRoom)
            }

            updatedRoom.toDto()
        }.flattenErrors().toRoomValidationResult()
    }

    private suspend fun findOtherRoomWithSameNumber(existentRoom: Room, patch: PatchRoomDto): Room? =
        patch.number
            ?.let { roomRepository.findByNumber(RoomNumber(it)) }
            ?.takeIf { it.id != existentRoom.id }
}

private fun Raise<NonEmptyList<RoomValidationError>>.ensureValidNumber(roomNumber: Int, existentRoomWithNumber: Room?) {
    accumulateErrors(
        { ensure(roomNumber >= RoomNumber.MIN_VALUE) { InvalidRoomNumber(roomNumber) } },
        { ensure(existentRoomWithNumber == null) { RoomNumberAlreadyExists(roomNumber) } },
    )
}

private fun Raise<NonEmptyList<RoomValidationError>>.ensureValidSeats(seats: NonEmptyList<SeatDto>) {
    accumulateErrors(
        { ensureValidSeatValues(seats) },
        { ensureNoDuplicatedSeats(seats) },
        { ensureNoMissingRows(seats) },
        { ensureNoMissingSeatNumbers(seats) },
    )
}

private fun Raise<NonEmptyList<RoomValidationError>>.ensureValidSeatValues(seats: NonEmptyList<SeatDto>) {
    accumulateErrors(seats) { seat ->
        accumulateErrors(
            { ensure(seat.row >= SeatRow.MIN_VALUE) { InvalidSeatRow(seat.row, seat.number) } },
            { ensure(seat.number >= SeatNumber.MIN_VALUE) { InvalidSeatNumber(seat.row, seat.number) } },
        )
    }
}

private fun Raise<NonEmptyList<DuplicatedSeat>>.ensureNoDuplicatedSeats(seats: NonEmptyList<SeatDto>) {
    val duplicatedSeats = seats.groupingBy { it }
        .eachCount()
        .filter { it.value > 1 }
        .keys

    accumulateErrors(duplicatedSeats) { seat ->
        raise(DuplicatedSeat(seat.row, seat.number))
    }
}

private fun Raise<NonEmptyList<MissingRow>>.ensureNoMissingRows(seats: NonEmptyList<SeatDto>) {
    val rows = seats.map { it.row }
    val missingRows = findMissing(rows)

    accumulateErrors(missingRows) { row ->
        raise(MissingRow(row))
    }
}

private fun Raise<NonEmptyList<MissingSeat>>.ensureNoMissingSeatNumbers(seats: NonEmptyList<SeatDto>) {
    val missingSeats = buildList {
        val seatsPerRow = seats.groupBy { it.row }
            .mapValues { (_, seats) -> seats.map { it.number } }

        for ((row, seats) in seatsPerRow) {
            val missingSeats = findMissing(seats).map { MissingSeat(row, it) }
            addAll(missingSeats)
        }
    }

    accumulateErrors(missingSeats) { missingSeat ->
        raise(missingSeat)
    }
}

private fun Either<NonEmptyList<RoomValidationError>, RoomDto>.toRoomValidationResult(): RoomValidationResult =
    fold(
        { errors -> RoomValidationResult.Failure(errors) },
        { room -> RoomValidationResult.Success(room) },
    )

private fun findMissing(numbers: List<Int>): List<Int> =
    buildList {
        val maxValue = numbers.max()
        val values = numbers.toSet()

        for (expectedValue in 1..maxValue) {
            if (expectedValue !in values) {
                add(expectedValue)
            }
        }
    }
