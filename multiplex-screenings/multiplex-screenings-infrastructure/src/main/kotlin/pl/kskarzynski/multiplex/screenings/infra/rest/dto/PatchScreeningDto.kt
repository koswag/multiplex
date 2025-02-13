@file:UseSerializers(LocalDateTimeSerializer::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.LocalDateTimeSerializer
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

@Serializable
data class PatchScreeningDto(
    val startTime: LocalDateTime?,
)

fun Screening.applyPatch(patch: PatchScreeningDto): Screening =
    copy(
        startTime = patch.startTime?.let { ScreeningStartTime(it) } ?: startTime,
    )
