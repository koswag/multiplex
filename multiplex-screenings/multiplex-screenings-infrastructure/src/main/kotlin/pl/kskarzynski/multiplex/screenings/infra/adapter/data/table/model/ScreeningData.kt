package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

data class ScreeningData(
    val id: ScreeningId,
    val movieId: MovieId,
    val roomId: RoomId,
    val startTime: ScreeningStartTime,
)

fun ScreeningData.toDomain(room: Room, bookings: List<Booking>) =
    Screening(
        id = id,
        movieId = movieId,
        room = room,
        startTime = startTime,
        bookings = bookings,
    )
