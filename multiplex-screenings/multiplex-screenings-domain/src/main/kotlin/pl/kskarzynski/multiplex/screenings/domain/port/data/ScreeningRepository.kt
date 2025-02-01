package pl.kskarzynski.multiplex.screenings.domain.port.data

import pl.kskarzynski.multiplex.screenings.domain.model.Screening

interface ScreeningRepository : ScreeningQueries {
    suspend fun save(screening: Screening)
}
