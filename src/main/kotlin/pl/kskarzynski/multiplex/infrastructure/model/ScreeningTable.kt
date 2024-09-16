package pl.kskarzynski.multiplex.infrastructure.model

import java.time.LocalDateTime
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.day
import org.jetbrains.exposed.sql.javatime.month
import org.jetbrains.exposed.sql.javatime.year
import pl.kskarzynski.multiplex.domain.model.screening.MovieTitle
import pl.kskarzynski.multiplex.domain.model.screening.RoomNumber
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningId
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningInfo
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningRoom
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningSummary

object ScreeningTable : UUIDTable("SCREENINGS") {
    val movieId = reference("MOVIE_ID", MovieTable)
    val roomId = reference("ROOM_ID", RoomTable)
    val startTime = datetime("START_TIME")

    context(Transaction)
    fun findScreeningsAfter(time: LocalDateTime): List<ScreeningSummary> =
        ScreeningTable.innerJoin(MovieTable)
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
                    title = MovieTitle(row[MovieTable.title]),
                    startTime = row[startTime],
                )
            }

    context(Transaction)
    fun findScreeningInfo(id: ScreeningId): ScreeningInfo? =
        ScreeningTable
            .innerJoin(MovieTable)
            .innerJoin(RoomTable)
            .select(MovieTable.title, RoomTable.id, startTime)
            .where { ScreeningTable.id eq id.value }
            .firstOrNull()
            ?.let { row ->
                ScreeningInfo(
                    id = id,
                    title = MovieTitle(row[MovieTable.title]),
                    startTime = row[startTime],
                    room = ScreeningRoom(
                        number = RoomNumber(row[RoomTable.id].value),
                        seats = emptyList(),  // TODO
                    )
                )
            }
}
