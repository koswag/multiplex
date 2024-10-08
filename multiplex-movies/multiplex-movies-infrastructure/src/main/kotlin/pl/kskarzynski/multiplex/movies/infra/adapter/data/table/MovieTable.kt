package pl.kskarzynski.multiplex.movies.infra.adapter.data.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.upsert
import pl.kskarzynski.multiplex.movies.api.model.Movie
import pl.kskarzynski.multiplex.movies.api.model.MovieReleaseYear
import pl.kskarzynski.multiplex.movies.api.model.MovieTitle
import pl.kskarzynski.multiplex.shared.movie.MovieId

object MovieTable : UUIDTable("multiplex_movies.movies") {
    val title = varchar("title", 255)
    val year = integer("year")

    fun upsert(movie: Movie) {
        upsert(id,
            onUpdate = listOf(
                title to stringLiteral(movie.title.value),
                year to intLiteral(movie.releaseYear.value),
            ),
        ) {
            it[id] = movie.id.value
            it[title] = movie.title.value
            it[year] = movie.releaseYear.value
        }
    }

    fun find(movieId: MovieId): Movie? =
        select(id, title, year)
            .where { id eq movieId.value }
            .firstOrNull()
            ?.let(::rowToDomain)

    fun rowToDomain(row: ResultRow): Movie =
        Movie(
            id = MovieId(row[id].value),
            title = MovieTitle(row[title]),
            releaseYear = MovieReleaseYear(row[year]),
        )
}
