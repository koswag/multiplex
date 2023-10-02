package pl.kskarzynski.multiplex.domain.model.screening

@JvmInline
value class SeatRow(val value: Int)

@JvmInline
value class SeatNumber(val value: Int)

@JvmInline
value class IsTaken(val value: Boolean)

data class Seat(
    val row: SeatRow,
    val number: SeatNumber,
    val isTaken: IsTaken,
)
