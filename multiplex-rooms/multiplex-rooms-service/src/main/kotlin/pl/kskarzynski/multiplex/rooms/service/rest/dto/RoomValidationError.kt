@file:OptIn(ExperimentalSerializationApi::class)

package pl.kskarzynski.multiplex.rooms.service.rest.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import pl.kskarzynski.multiplex.shared.room.RoomNumber
import pl.kskarzynski.multiplex.shared.room.SeatNumber
import pl.kskarzynski.multiplex.shared.room.SeatRow

// FIXME: Missing type discriminator
// TODO: Duplicated seats validation
// TODO: Missing rows validation
// TODO: Missing seats validation
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
}
