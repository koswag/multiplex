package pl.kskarzynski.multiplex.api.config

import java.time.Clock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.kskarzynski.multiplex.domain.policy.BookingExpirationPolicy
import pl.kskarzynski.multiplex.domain.policy.BookingPricingPolicy
import pl.kskarzynski.multiplex.domain.repo.BookingRepository
import pl.kskarzynski.multiplex.domain.usecase.BookScreeningUseCase

@Configuration
class MultiplexComponentConfig {

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun bookScreeningUseCase(
        bookingPricingPolicy: BookingPricingPolicy,
        bookingExpirationPolicy: BookingExpirationPolicy,
        bookingRepository: BookingRepository,
    ) = BookScreeningUseCase(bookingPricingPolicy, bookingExpirationPolicy, bookingRepository)

    @Bean
    fun bookingPricingPolicy() = BookingPricingPolicy()

    @Bean
    fun bookingExpirationPolicy() = BookingExpirationPolicy()
}
