package pl.kskarzynski.multiplex.movies.service.data

import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import pl.kskarzynski.multiplex.movies.service.data.table.MovieTable
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId

class DatabaseMovieRepository : MovieRepository {

    override suspend fun save(movie: Movie) {
        suspendTransaction {
            MovieTable.save(movie)
        }
    }

    override suspend fun findById(movieId: MovieId): Movie? =
        suspendTransaction {
            MovieTable.find(movieId)
        }
}
