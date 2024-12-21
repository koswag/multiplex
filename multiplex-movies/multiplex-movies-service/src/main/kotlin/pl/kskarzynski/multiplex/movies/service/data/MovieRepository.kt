package pl.kskarzynski.multiplex.movies.service.data

import pl.kskarzynski.multiplex.shared.movie.Movie

interface MovieRepository : MovieQueries {
    suspend fun save(movie: Movie)
}
