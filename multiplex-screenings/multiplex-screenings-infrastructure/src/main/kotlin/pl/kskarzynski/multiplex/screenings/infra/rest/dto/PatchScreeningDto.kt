package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import java.time.LocalDateTime
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

data class PatchScreeningDto(
    val startTime: LocalDateTime?,
)

fun Screening.applyPatch(patch: PatchScreeningDto): Screening =
    copy(
        startTime = patch.startTime?.let { ScreeningStartTime(it) } ?: startTime,
    )
