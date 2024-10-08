package pl.kskarzynski.multiplex.movies.api.model

@JvmInline
value class MovieTitle(val value: String) {
    init {
        require(value.isNotEmpty()) { "Movie title must not be empty." }
    }
}
