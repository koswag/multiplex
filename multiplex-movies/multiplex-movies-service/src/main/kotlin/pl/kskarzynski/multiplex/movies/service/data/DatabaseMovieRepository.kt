package pl.kskarzynski.multiplex.movies.service.data

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pl.kskarzynski.multiplex.movies.service.data.table.MovieTable
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId

class DatabaseMovieRepository : MovieRepository {

    override suspend fun save(movie: Movie) {
        newSuspendedTransaction {
            MovieTable.upsert(movie)
        }
    }

    override suspend fun findById(movieId: MovieId): Movie? =
        newSuspendedTransaction {
            MovieTable.find(movieId)
        }
}
