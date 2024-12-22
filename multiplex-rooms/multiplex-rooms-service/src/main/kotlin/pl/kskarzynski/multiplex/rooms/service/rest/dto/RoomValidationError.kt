package pl.kskarzynski.multiplex.rooms.service.rest.dto

import kotlinx.serialization.Serializable
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

@Serializable
sealed interface RoomValidationError {

    @Serializable
    data class RoomNumberAlreadyExists(val number: Int) : RoomValidationError

    @Serializable
    sealed interface SeatValidationError : RoomValidationError {

        @Serializable
        data class InvalidSeatRow(val seatRow: Int, val seatNumber: Int) : SeatValidationError {
            val minValue = SeatRow.MIN_VALUE
        }

        @Serializable
        data class InvalidSeatNumber(val seatRow: Int, val seatNumber: Int) : SeatValidationError {
            val minValue = SeatNumber.MIN_VALUE
        }
    }
}
