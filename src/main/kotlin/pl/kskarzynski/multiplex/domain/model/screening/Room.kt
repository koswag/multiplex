package pl.kskarzynski.multiplex.domain.model.screening

@JvmInline
value class RoomNumber(val value: Int)

data class Room(
    val number: RoomNumber,
    val seats: Collection<Seat>,
)
