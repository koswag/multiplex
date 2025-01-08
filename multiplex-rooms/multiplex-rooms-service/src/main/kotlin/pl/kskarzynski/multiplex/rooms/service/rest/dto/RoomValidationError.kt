@file:OptIn(ExperimentalSerializationApi::class)

package pl.kskarzynski.multiplex.rooms.service.rest.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

@Serializable
sealed class RoomValidationError {

    @Serializable
    data class RoomNumberAlreadyExists(val number: Int) : RoomValidationError()

    @Serializable
    data class InvalidRoomNumber(val number: Int) : RoomValidationError() {
        val minValue = RoomNumber.MIN_VALUE
    }

    @Serializable
    data class InvalidSeatRow(val seatRow: Int, val seatNumber: Int) : RoomValidationError() {
        val minValue = SeatRow.MIN_VALUE
    }

    @Serializable
    data class InvalidSeatNumber(val seatRow: Int, val seatNumber: Int) : RoomValidationError() {
        val minValue = SeatNumber.MIN_VALUE
    }

    @Serializable
    data class DuplicatedSeat(val seatRow: Int, val seatNumber: Int) : RoomValidationError()

    @Serializable
    data class MissingRow(val row: Int) : RoomValidationError()

    @Serializable
    data class MissingSeat(val seatRow: Int, val seatNumber: Int) : RoomValidationError()
}
