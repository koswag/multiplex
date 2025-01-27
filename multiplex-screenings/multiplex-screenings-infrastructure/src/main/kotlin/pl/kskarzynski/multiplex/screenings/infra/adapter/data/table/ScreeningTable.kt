package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.ScreeningData
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

// TODO: Tests
internal object ScreeningTable : UUIDTable("multiplex_screenings.screenings") {
    val movieId = uuid("movie_id")
    val roomId = uuid("room_id")
    val startTime = datetime("start_time")

    fun find(screeningId: ScreeningId): ScreeningData? =
        selectAll()
            .where { id eq screeningId.value }
            .firstOrNull()
            ?.let { rowToScreeningData(it) }

    fun findAll(screeningIds: Collection<ScreeningId>): List<ScreeningData> =
        selectAll()
            .where { id inList screeningIds.map { it.value } }
            .map { rowToScreeningData(it) }

    fun save(screening: Screening) {
        upsert(id) {
            it[id] = screening.id.value
            it[movieId] = screening.movieId.value
            it[roomId] = screening.room.id.value
            it[startTime] = screening.startTime.value
        }
    }

    private fun rowToScreeningData(row: ResultRow): ScreeningData {
        val screeningId = ScreeningId(row[id].value)

        return ScreeningData(
            id = screeningId,
            movieId = MovieId(row[movieId]),
            roomId = RoomId(row[roomId]),
            startTime = ScreeningStartTime(row[startTime]),
        )
    }
}
