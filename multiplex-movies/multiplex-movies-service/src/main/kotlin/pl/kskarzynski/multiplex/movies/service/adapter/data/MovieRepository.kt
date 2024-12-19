package pl.kskarzynski.multiplex.movies.service.adapter.data

import pl.kskarzynski.multiplex.shared.movie.Movie

interface MovieRepository : MovieQueries {
    suspend fun save(movie: Movie)
}
