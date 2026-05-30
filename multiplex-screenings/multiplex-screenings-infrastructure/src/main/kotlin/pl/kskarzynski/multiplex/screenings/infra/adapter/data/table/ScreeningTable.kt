@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.upsert
import pl.kskarzynski.multiplex.common.infra.exposed.page
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.model.request.MovieScreeningSearchRequest
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.ScreeningData
import pl.kskarzynski.multiplex.shared.misc.AggregateVersion
import pl.kskarzynski.multiplex.shared.misc.Page
import pl.kskarzynski.multiplex.shared.misc.StaleAggregateVersionException
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime
import kotlin.uuid.ExperimentalUuidApi

object ScreeningTable : UuidTable("multiplex_screenings.screenings") {
    val movieId = uuid("movie_id")
    val roomId = uuid("room_id")
    val startTime = datetime("start_time")
    val version = long("version")

    suspend fun find(screeningId: ScreeningId): ScreeningData? =
        selectAll()
            .where { id eq screeningId.value }
            .firstOrNull()
            ?.let { rowToScreeningData(it) }

    fun findAll(screeningIds: Collection<ScreeningId>): Flow<ScreeningData> =
        selectAll()
            .where { id inList screeningIds.map { it.value } }
            .map { rowToScreeningData(it) }

    suspend fun findScreeningsByMovie(request: MovieScreeningSearchRequest): Page<ScreeningData> {
        val query = selectAll()
            .where { movieId eq request.movieId.value }
            .orderBy(startTime)

        request.minStartTime?.let { minStartTime ->
            query.andWhere { startTime greaterEq minStartTime.value }
        }

        request.maxStartTime?.let { maxStartTime ->
            query.andWhere { startTime lessEq maxStartTime.value }
        }

        return query.page(request.pagingRequest) { rowToScreeningData(it) }
    }

    suspend fun save(screening: Screening) {
        findCurrentVersion(screening.id)?.let { currentVersion ->
            if (screening.version != currentVersion) {
                throw StaleAggregateVersionException("The Screening state has already been changed.")
            }
        }

        upsert(id) {
            it[id] = screening.id.value
            it[movieId] = screening.movieId.value
            it[roomId] = screening.room.id.value
            it[startTime] = screening.startTime.value
            it[version] = screening.version.increment().value
        }
    }

    private suspend fun findCurrentVersion(screeningId: ScreeningId): AggregateVersion? =
        select(version).where { id eq screeningId.value }
            .firstOrNull()
            ?.let { row -> AggregateVersion(row[version]) }

    private fun rowToScreeningData(row: ResultRow) =
        ScreeningData(
            id = ScreeningId(row[id].value),
            movieId = MovieId(row[movieId]),
            roomId = RoomId(row[roomId]),
            startTime = ScreeningStartTime(row[startTime]),
            version = AggregateVersion(row[version]),
        )
}
