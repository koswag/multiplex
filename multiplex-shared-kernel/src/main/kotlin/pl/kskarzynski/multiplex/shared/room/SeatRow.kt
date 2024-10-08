package pl.kskarzynski.multiplex.shared.room

@JvmInline
value class SeatRow(val value: Int) {
    init {
        require(value >= MIN_VALUE) { "The minimal value of a seat row is $MIN_VALUE: '$value'" }
    }

    companion object {
        const val MIN_VALUE = 1
    }
}
