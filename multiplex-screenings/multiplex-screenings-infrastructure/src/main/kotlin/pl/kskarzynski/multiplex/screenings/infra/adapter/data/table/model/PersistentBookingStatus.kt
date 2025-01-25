package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.ConfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.ExpiredBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus.CONFIRMED
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus.EXPIRED
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus.UNCONFIRMED

internal enum class PersistentBookingStatus {
    UNCONFIRMED,
    EXPIRED,
    CONFIRMED,
}

internal val Booking.status: PersistentBookingStatus
    get() = when (this) {
        is UnconfirmedBooking -> UNCONFIRMED
        is ExpiredBooking -> EXPIRED
        is ConfirmedBooking -> CONFIRMED
    }
