package pl.kskarzynski.multiplex.shared.misc

@JvmInline
value class AggregateVersion(val value: Long) {
    init {
        require(value >= 0) { "Aggregate Version must not be negative: '$value'" }
    }

    override fun toString() = value.toString()

    fun increment() = AggregateVersion(this.value + 1)

    companion object {
        val ZERO = AggregateVersion(0)
    }
}
