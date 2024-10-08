package pl.kskarzynski.multiplex.shared.room

data class Room(
    val id: RoomId,
    val number: RoomNumber,
    val seats: List<Seat>,
)
