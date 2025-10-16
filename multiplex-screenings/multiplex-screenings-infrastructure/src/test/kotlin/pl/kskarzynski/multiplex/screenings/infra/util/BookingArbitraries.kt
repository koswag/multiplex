package pl.kskarzynski.multiplex.screenings.infra.util

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.string
import java.time.LocalDateTime
import pl.kskarzynski.multiplex.common.test.arbs.bookingId
import pl.kskarzynski.multiplex.common.test.arbs.seat
import pl.kskarzynski.multiplex.common.utils.arrow.toNonEmptyList
import pl.kskarzynski.multiplex.screenings.domain.model.booking.Booking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.ConfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.ExpiredBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.Ticket
import pl.kskarzynski.multiplex.screenings.domain.model.booking.ticket.TicketType
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserInfo
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserName
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserSurname
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserValidationConstants.HYPHEN
import pl.kskarzynski.multiplex.shared.booking.BookingConfirmationTime
import pl.kskarzynski.multiplex.shared.booking.BookingExpirationTime
import pl.kskarzynski.multiplex.shared.booking.BookingPrice
import pl.kskarzynski.multiplex.shared.booking.BookingTime
import pl.kskarzynski.multiplex.shared.room.Seat

fun Arb.Companion.booking(
    bookingTime: Arb<LocalDateTime> = Arb.localDateTime(),
    expirationTime: Arb<LocalDateTime>? = null,
    confirmationTime: Arb<LocalDateTime>? = null,
    seats: Collection<Seat> = emptyList(),
): Arb<Booking> =
    Arb.choice(
        Arb.unconfirmedBooking(bookingTime, expirationTime, seats),
        Arb.expiredBooking(bookingTime, expirationTime, seats),
        Arb.confirmedBooking(bookingTime, expirationTime, confirmationTime, seats),
    )

fun Arb.Companion.unconfirmedBooking(
    bookingTime: Arb<LocalDateTime> = Arb.localDateTime(),
    expirationTime: Arb<LocalDateTime>? = null,
    seats: Collection<Seat> = emptyList(),
): Arb<UnconfirmedBooking> =
    arbitrary {
        val id = Arb.bookingId().bind()
        val userInfo = Arb.userInfo().bind()
        val tickets = Arb.list(Arb.ticket(seats), 1..10).bind().distinct().toNonEmptyList()
        val time = BookingTime(bookingTime.bind())
        val expiration = BookingExpirationTime(expirationTime?.bind() ?: time.value.plusDays(15))
        val totalPrice = BookingPrice(tickets.sumOf { it.basePrice.value })

        UnconfirmedBooking(id, userInfo, tickets, time, expiration, totalPrice)
    }

fun Arb.Companion.expiredBooking(
    bookingTime: Arb<LocalDateTime> = Arb.localDateTime(),
    expirationTime: Arb<LocalDateTime>? = null,
    seats: Collection<Seat> = emptyList(),
): Arb<ExpiredBooking> =
    arbitrary {
        val id = Arb.bookingId().bind()
        val userInfo = Arb.userInfo().bind()
        val tickets = Arb.list(Arb.ticket(seats), 1..10).bind().distinct().toNonEmptyList()
        val time = BookingTime(bookingTime.bind())
        val expiration = BookingExpirationTime(expirationTime?.bind() ?: time.value.plusDays(15))

        ExpiredBooking(id, userInfo, tickets, time, expiration)
    }

fun Arb.Companion.confirmedBooking(
    bookingTime: Arb<LocalDateTime> = Arb.localDateTime(),
    expirationTime: Arb<LocalDateTime>? = null,
    confirmationTime: Arb<LocalDateTime>? = null,
    seats: Collection<Seat> = emptyList(),
): Arb<ConfirmedBooking> =
    arbitrary {
        val id = Arb.bookingId().bind()
        val userInfo = Arb.userInfo().bind()
        val tickets = Arb.list(Arb.ticket(seats), 1..10).bind().distinct().toNonEmptyList()
        val time = BookingTime(bookingTime.bind())
        val expiration = BookingExpirationTime(expirationTime?.bind() ?: time.value.plusDays(15))
        val totalPrice = BookingPrice(tickets.sumOf { it.basePrice.value })
        val confirmation = BookingConfirmationTime(confirmationTime?.bind() ?: time.value.plusDays(10))

        ConfirmedBooking(id, userInfo, tickets, time, expiration, totalPrice, confirmation)
    }

fun Arb.Companion.userInfo(): Arb<UserInfo> =
    arbitrary {
        val name = Arb.userName().bind()
        val surname = Arb.userSurname().bind()

        UserInfo(name, surname)
    }

fun Arb.Companion.userName(): Arb<UserName> =
    Arb.string(
        minSize = UserName.MIN_LENGTH,
        maxSize = 10,
        codepoints = Arb.of(UserName.VALID_CHARACTERS)
            .map { Codepoint(it.lowercaseChar().code) }
    ).map { lowercaseName ->
        UserName(lowercaseName.replaceFirstChar { it.uppercaseChar() })
    }

fun Arb.Companion.userSurname(): Arb<UserSurname> =
    Arb.choice(
        Arb.onePartUserSurname(),
        Arb.twoPartUserSurname(),
    )

fun Arb.Companion.twoPartUserSurname(): Arb<UserSurname> =
    arbitrary {
        val firstPart = Arb.onePartUserSurname().bind()
        val secondPart = Arb.onePartUserSurname().bind()

        UserSurname("${firstPart.value}-${secondPart.value}")
    }

fun Arb.Companion.onePartUserSurname(): Arb<UserSurname> =
    Arb.string(
        minSize = UserSurname.MIN_LENGTH,
        maxSize = 10,
        codepoints = Arb.of(UserSurname.VALID_CHARACTERS - HYPHEN)
            .map { Codepoint(it.lowercaseChar().code) },
    ).map { lowercaseSurname ->
        UserSurname(lowercaseSurname.replaceFirstChar { it.uppercaseChar() })
    }

fun Arb.Companion.ticket(
    seats: Collection<Seat> = emptyList(),
): Arb<Ticket> =
    arbitrary {
        val type = Arb.enum<TicketType>().bind()
        val seat =
            if (seats.isNotEmpty()) {
                Arb.of(seats).bind()
            } else {
                Arb.seat().bind()
            }

        Ticket(type, seat)
    }
