package pl.kskarzynski.multiplex.movies.domain.port

import pl.kskarzynski.multiplex.shared.movie.Movie

interface MovieRepository : MovieQueries {
    suspend fun saveMovie(movie: Movie)
}
