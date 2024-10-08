package pl.kskarzynski.multiplex.movies.domain.port

import pl.kskarzynski.multiplex.movies.api.model.Movie

interface MovieRepository : MovieQueries {
    suspend fun saveMovie(movie: Movie)
}
