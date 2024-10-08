package pl.kskarzynski.multiplex.shared.movie

@JvmInline
value class MovieTitle(val value: String) {
    init {
        require(value.isNotEmpty()) { "Movie title must not be empty." }
    }
}
