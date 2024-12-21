package pl.kskarzynski.multiplex.shared.movie

@JvmInline
value class MovieReleaseYear(val value: Int) {
    init {
        require(value >= MIN_VALUE) { "MovieReleaseYear must be greater or equal to $MIN_VALUE" }
    }

    companion object {
        const val MIN_VALUE = 1888
    }
}
