@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.movies.service.data.table

import kotlinx.coroutines.flow.firstOrNull
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.upsert
import pl.kskarzynski.multiplex.shared.movie.Movie
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.movie.MovieReleaseYear
import pl.kskarzynski.multiplex.shared.movie.MovieTitle
import kotlin.uuid.ExperimentalUuidApi

object MovieTable : UuidTable("multiplex_movies.movies") {
    val title = varchar("title", 255)
    val year = integer("year")

    suspend fun save(movie: Movie) {
        upsert(id) {
            it[id] = movie.id.value
            it[title] = movie.title.value
            it[year] = movie.releaseYear.value
        }
    }

    suspend fun find(movieId: MovieId): Movie? =
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
