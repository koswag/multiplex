package pl.kskarzynski.multiplex.screenings.domain.port.usecase

import java.time.Clock
import pl.kskarzynski.multiplex.common.utils.datetime.currentTime
import pl.kskarzynski.multiplex.screenings.domain.model.Screening
import pl.kskarzynski.multiplex.screenings.domain.port.data.ScreeningRepository

class CancelExpiredBookingsUseCase(
    private val screeningRepository: ScreeningRepository,
    private val clock: Clock,
) {
    suspend fun execute(screening: Screening) {
        val updatedScreening = screening.cancelExpiredBookings(clock.currentTime())
        screeningRepository.saveScreening(updatedScreening)
    }
}
