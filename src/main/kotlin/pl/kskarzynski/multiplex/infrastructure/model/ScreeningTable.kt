package pl.kskarzynski.multiplex.infrastructure.model

import java.time.LocalDateTime
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.day
import org.jetbrains.exposed.sql.javatime.month
import org.jetbrains.exposed.sql.javatime.year
import pl.kskarzynski.multiplex.domain.model.screening.Screening
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningSummary
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

object ScreeningTable : UUIDTable("SCREENINGS") {
    val movieId = uuid("MOVIE_ID")
    val roomId = reference("ROOM_ID", ScreeningRoomTable)
    val startTime = datetime("START_TIME")

    context(Transaction)
    fun findScreeningsAfter(time: LocalDateTime): List<ScreeningSummary> =
        ScreeningTable
            .select(id, startTime, MovieTable.title)
            .where {
                (startTime greater time) and
                    (startTime.year() eq time.year) and
                    (startTime.month() eq time.month.value) and
                    (startTime.day() eq time.dayOfMonth)
            }
            .map { row ->
                ScreeningSummary(
                    id = ScreeningId(row[id].value),
                    movieId = MovieId(row[movieId]),
                    roomId = RoomId(row[roomId].value),
                    startTime = row[startTime],
                )
            }

    context(Transaction)
    fun findScreening(id: ScreeningId): Screening? =
        ScreeningTable
            .innerJoin(ScreeningRoomTable)
            .select(
                movieId,
                startTime,
                ScreeningRoomTable.id,
                ScreeningRoomTable.number,
            )
            .where { ScreeningTable.id eq id.value }
            .firstOrNull()
            ?.let { row ->
                Screening(
                    id = id,
                    movieId = MovieId(row[movieId]),
                    startTime = row[startTime],
                    room = ScreeningRoomTable.rowToDomain(row),
                )
            }
}
