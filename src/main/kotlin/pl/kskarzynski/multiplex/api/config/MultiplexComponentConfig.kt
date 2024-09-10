package pl.kskarzynski.multiplex.api.config

import java.time.Clock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MultiplexComponentConfig {

    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
