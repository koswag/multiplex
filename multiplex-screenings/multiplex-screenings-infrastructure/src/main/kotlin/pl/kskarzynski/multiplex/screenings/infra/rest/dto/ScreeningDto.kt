@file:UseSerializers(UuidSerializer::class, LocalDateTimeSerializer::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.LocalDateTimeSerializer
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.shared.movie.Movie

@Serializable
data class ScreeningDto(
    val id: UUID,
    val movie: ScreeningMovieDto,
    val room: ScreeningRoomDto,
    val startTime: LocalDateTime,
)

fun Screening.toDto(movie: Movie) =
    ScreeningDto(
        id = id.value,
        movie = movie.toDto(),
        room = this.toScreeningRoomDto(),
        startTime = startTime.value,
    )
