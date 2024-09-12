package pl.kskarzynski.multiplex.domain.model.screening

@JvmInline
value class SeatNumber(val value: Int) {
    init {
        require(value >= MIN_VALUE) { "The minimal value of a seat placement is $MIN_VALUE: '$value'" }
    }

    companion object {
        const val MIN_VALUE = 1
    }
}
