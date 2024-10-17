package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table

import java.time.LocalDateTime
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.dateTimeLiteral
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.upsert
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.ScreeningData
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

object ScreeningTable : UUIDTable("multiplex_screenings.screenings") {
    val movieId = uuid("movie_id")
    val roomId = uuid("room_id")
    val startTime = datetime("start_time")

    fun findScreening(screeningId: ScreeningId): ScreeningData? =
        select(id, movieId, roomId, startTime)
            .where { id eq screeningId.value }
            .firstOrNull()
            ?.let { rowToScreeningData(it) }

    fun saveScreening(screening: Screening) {
        upsert(id,
            onUpdate = listOf(
                movieId to stringLiteral(screening.movieId.value.toString()),
                startTime to dateTimeLiteral(screening.startTime.value),
            )
        ) {
            it[id] = screening.id.value
            it[movieId] = screening.movieId.value
            it[startTime] = screening.startTime.value
        }

        for (booking in screening.bookings) {
            BookingTable.saveBooking(screening.id, booking)
        }
    }

    // TODO: Optimize
    fun findScreeningsWithExpiredBookings(currentTime: LocalDateTime): List<ScreeningData> {
        val screeningIds =
            BookingTable.findExpiredBookingScreeningIds(currentTime)
                .map { it.value }

        return select(id, movieId, roomId, startTime)
            .where { id inList screeningIds }
            .map { rowToScreeningData(it) }
    }

    fun findScreeningByBooking(bookingId: BookingId): ScreeningData? {
        val screeningId = BookingTable.findScreeningIdByBooking(bookingId)
            ?: return null

        return findScreening(screeningId)
    }

    private fun rowToScreeningData(row: ResultRow): ScreeningData {
        val screeningId = ScreeningId(row[id].value)

        return ScreeningData(
            id = screeningId,
            movieId = MovieId(row[movieId]),
            roomId = RoomId(row[roomId]),
            startTime = ScreeningStartTime(row[startTime]),
            bookings = BookingTable.findBookings(screeningId),
        )
    }
}
