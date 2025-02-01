package pl.kskarzynski.multiplex.screenings.infra.config

import org.koin.dsl.module
import pl.kskarzynski.multiplex.screenings.domain.port.data.ScreeningRepository
import pl.kskarzynski.multiplex.screenings.domain.port.policy.BookingExpirationPolicy
import pl.kskarzynski.multiplex.screenings.domain.port.policy.BookingExpirationPolicyImpl
import pl.kskarzynski.multiplex.screenings.domain.port.policy.BookingPricingPolicy
import pl.kskarzynski.multiplex.screenings.domain.port.policy.BookingPricingPolicyImpl
import pl.kskarzynski.multiplex.screenings.domain.port.usecase.BookScreeningUseCase
import pl.kskarzynski.multiplex.screenings.domain.port.usecase.CancelExpiredBookingsUseCase
import pl.kskarzynski.multiplex.screenings.domain.port.usecase.ConfirmBookingUseCase
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.DatabaseScreeningRepository
import pl.kskarzynski.multiplex.screenings.infra.rest.ScreeningRestService

val ScreeningModule = module {
    single<ScreeningRepository> { DatabaseScreeningRepository(get(), get()) }
    single<BookingPricingPolicy> { BookingPricingPolicyImpl() }
    single<BookingExpirationPolicy> { BookingExpirationPolicyImpl() }
    single { BookScreeningUseCase(get(), get(), get()) }
    single { CancelExpiredBookingsUseCase(get(), get()) }
    single { ConfirmBookingUseCase(get(), get()) }
    single { ScreeningRestService(get(), get(), get(), get(), get()) }
}
