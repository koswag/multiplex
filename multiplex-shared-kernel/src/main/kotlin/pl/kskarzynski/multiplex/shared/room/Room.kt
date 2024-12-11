package pl.kskarzynski.multiplex.shared.room

import arrow.core.NonEmptyList

data class Room(
    val id: RoomId,
    val number: RoomNumber,
    val seats: NonEmptyList<Seat>,
)
