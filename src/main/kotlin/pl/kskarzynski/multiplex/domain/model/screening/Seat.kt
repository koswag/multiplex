package pl.kskarzynski.multiplex.domain.model.screening

@JvmInline
value class SeatRow(val value: Int)

@JvmInline
value class SeatNumber(val value: Int)

data class SeatPlacement(
    val row: SeatRow,
    val number: SeatNumber,
)

data class Seat(
    val placement: SeatPlacement,
    val isTaken: Boolean,
) {
    val isAvailable: Boolean = !isTaken
}
