package pl.kskarzynski.multiplex.screenings.infra.rest

import arrow.core.NonEmptyList
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningValidationErrorDto

sealed interface ScreeningValidationResult {
    data class Success(val screening: ScreeningDto): ScreeningValidationResult
    data class Failure(val errors: NonEmptyList<ScreeningValidationErrorDto>) : ScreeningValidationResult
}