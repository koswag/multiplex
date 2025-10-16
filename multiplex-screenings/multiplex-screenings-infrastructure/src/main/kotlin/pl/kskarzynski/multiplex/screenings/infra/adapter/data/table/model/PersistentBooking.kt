package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import arrow.core.NonEmptyList
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.ConfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.ExpiredBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus.CONFIRMED
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus.EXPIRED
import pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model.PersistentBookingStatus.UNCONFIRMED
import pl.kskarzynski.multiplex.shared.booking.BookingConfirmationTime
import pl.kskarzynski.multiplex.shared.booking.BookingExpirationTime
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.booking.BookingPrice
import pl.kskarzynski.multiplex.shared.booking.BookingTime
import pl.kskarzynski.multiplex.shared.screening.ScreeningId

data class PersistentBooking(
    val id: UUID,
    val status: PersistentBookingStatus,
    val screeningId: UUID,
    val userInfo: PersistentUserInfo,
    val tickets: NonEmptyList<PersistentTicket>,
    val bookingTime: LocalDateTime,
    val expirationTime: LocalDateTime,
    val totalPrice: BigDecimal?,
    val confirmationTime: LocalDateTime?,
)

fun PersistentBooking.toDomain(): Booking =
    when (status) {
        UNCONFIRMED -> UnconfirmedBooking(
            id = BookingId(id),
            userInfo = userInfo.toDomain(),
            tickets = tickets.map { it.toDomain() },
            bookingTime = BookingTime(bookingTime),
            expirationTime = BookingExpirationTime(expirationTime),
            totalPrice = BookingPrice(totalPrice ?: error("Unconfirmed booking $id has no price")),
        )
        EXPIRED -> ExpiredBooking(
            id = BookingId(id),
            userInfo = userInfo.toDomain(),
            tickets = tickets.map { it.toDomain() },
            bookingTime = BookingTime(bookingTime),
            expirationTime = BookingExpirationTime(expirationTime),
        )
        CONFIRMED -> ConfirmedBooking(
            id = BookingId(id),
            userInfo = userInfo.toDomain(),
            tickets = tickets.map { it.toDomain() },
            bookingTime = BookingTime(bookingTime),
            expirationTime = BookingExpirationTime(expirationTime),
            totalPrice = BookingPrice(totalPrice ?: error("Confirmed booking $id has no price")),
            confirmationTime = BookingConfirmationTime(
                confirmationTime ?: error("Confirmed booking $id has no confirmation time")
            )
        )
    }

fun Booking.toPersistentBooking(screeningId: ScreeningId) =
    PersistentBooking(
        id = id.value,
        status = status,
        screeningId = screeningId.value,
        userInfo = userInfo.toPersistentUserInfo(),
        tickets = tickets.map { it.toPersistentTicket() },
        bookingTime = bookingTime.value,
        expirationTime = expirationTime.value,
        totalPrice = totalPrice?.value,
        confirmationTime = confirmationTime?.value,
    )

private val Booking.totalPrice: BookingPrice?
    get() = when (this) {
        is UnconfirmedBooking -> totalPrice
        is ConfirmedBooking -> totalPrice
        is ExpiredBooking -> null
    }

private val Booking.confirmationTime: BookingConfirmationTime?
    get() = when (this) {
        is ConfirmedBooking -> confirmationTime
        is UnconfirmedBooking, is ExpiredBooking -> null
    }
