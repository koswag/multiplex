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

val ScreeningsModule =
    module {
        single<ScreeningRepository> { DatabaseScreeningRepository(get(), get()) }
        single<BookingPricingPolicy> { BookingPricingPolicyImpl() }
        single<BookingExpirationPolicy> { BookingExpirationPolicyImpl() }
        single { BookScreeningUseCase(get(), get(), get()) }
        single { CancelExpiredBookingsUseCase(get(), get()) }
        single { ConfirmBookingUseCase(get(), get()) }
    }
