package pl.kskarzynski.multiplex.domain.services.expiration

import pl.kskarzynski.multiplex.domain.model.booking.BookingExpirationTime
import java.time.LocalDateTime

interface BookingExpirationPolicy {

    companion object {
        const val BOOKING_LIFESPAN_IN_MINUTES = 15
    }

    operator fun invoke(currTime: LocalDateTime): BookingExpirationTime =
        BookingExpirationTime(
            currTime.plusMinutes(BOOKING_LIFESPAN_IN_MINUTES.toLong())
        )

}
