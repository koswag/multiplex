package pl.kskarzynski.multiplex.shared.room

@JvmInline
value class RoomNumber(val value: Int) {
    init {
        require(value >= MIN_VALUE) { "The minimal value of a room number is $MIN_VALUE" }
    }

    companion object {
        const val MIN_VALUE = 1
    }
}
