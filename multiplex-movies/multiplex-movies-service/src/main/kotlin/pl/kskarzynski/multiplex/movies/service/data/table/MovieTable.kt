package pl.kskarzynski.multiplex.movies.service.data.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.upsert
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.movie.MovieReleaseYear
import pl.kskarzynski.multiplex.shared.movie.MovieTitle

object MovieTable : UUIDTable("multiplex_movies.movies") {
    val title = varchar("title", 255)
    val year = integer("year")

    fun save(movie: Movie) {
        upsert(id) {
            it[id] = movie.id.value
            it[title] = movie.title.value
            it[year] = movie.releaseYear.value
        }
    }

    fun find(movieId: MovieId): Movie? =
        select(id, title, year)
            .where { id eq movieId.value }
            .firstOrNull()
            ?.let { rowToDomain(it) }

    fun rowToDomain(row: ResultRow): Movie =
        Movie(
            id = MovieId(row[id].value),
            title = MovieTitle(row[title]),
            releaseYear = MovieReleaseYear(row[year]),
        )
}
