package pl.kskarzynski.multiplex.movies.infra.adapter.data

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pl.kskarzynski.multiplex.movies.api.model.Movie
import pl.kskarzynski.multiplex.movies.domain.port.MovieRepository
import pl.kskarzynski.multiplex.movies.infra.adapter.data.table.MovieTable
import pl.kskarzynski.multiplex.shared.movie.MovieId

class DatabaseMovieRepository : MovieRepository {

    override suspend fun saveMovie(movie: Movie) {
        newSuspendedTransaction {
            MovieTable.upsert(movie)
        }
    }

    override suspend fun findMovie(movieId: MovieId): Movie? =
        newSuspendedTransaction {
            MovieTable.find(movieId)
        }
}
