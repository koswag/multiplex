package pl.kskarzynski.multiplex.rooms.service.rest

import arrow.core.NonEmptyList
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomDto
import pl.kskarzynski.multiplex.rooms.service.rest.dto.RoomValidationError

sealed interface RoomValidationResult {
    data class Success(val room: RoomDto) : RoomValidationResult
    data class Failure(val errors: NonEmptyList<RoomValidationError>) : RoomValidationResult
}
