package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.room.Room
import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

// TODO: Better naming?
data class ScreeningData(
    val id: ScreeningId,
    val movieId: MovieId,
    val roomId: RoomId,
    val startTime: ScreeningStartTime,
    val bookings: List<Booking>,
) {
    fun toDomain(room: Room) =
        Screening(id, movieId, room, startTime, bookings)
}
