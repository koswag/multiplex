package pl.kskarzynski.multiplex.infrastructure.model

import arrow.core.nonEmptyListOf
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import pl.kskarzynski.multiplex.domain.model.booking.Booking
import pl.kskarzynski.multiplex.domain.model.booking.Booking.CancelledBooking
import pl.kskarzynski.multiplex.domain.model.booking.Booking.ConfirmedBooking
import pl.kskarzynski.multiplex.domain.model.booking.Booking.UnconfirmedBooking
import pl.kskarzynski.multiplex.domain.model.booking.BookingExpirationTime
import pl.kskarzynski.multiplex.domain.model.booking.BookingId
import pl.kskarzynski.multiplex.domain.model.booking.BookingPrice
import pl.kskarzynski.multiplex.domain.model.booking.BookingTime
import pl.kskarzynski.multiplex.domain.model.screening.ScreeningId
import pl.kskarzynski.multiplex.domain.model.screening.SeatNumber
import pl.kskarzynski.multiplex.domain.model.screening.SeatPlacement
import pl.kskarzynski.multiplex.domain.model.screening.SeatRow
import pl.kskarzynski.multiplex.domain.model.ticket.Ticket
import pl.kskarzynski.multiplex.domain.model.ticket.TicketType
import pl.kskarzynski.multiplex.domain.model.user.UserInfo
import pl.kskarzynski.multiplex.domain.model.user.UserName
import pl.kskarzynski.multiplex.domain.model.user.UserSurname

object BookingTable : UUIDTable("BOOKINGS") {
    val userName = varchar("USER_NAME", length = 64)
    val userSurname = varchar("USER_SURNAME", length = 64)
    val screeningId = reference("SCREENING_ID", ScreeningTable)
    val bookingTime = datetime("BOOKING_TIME")
    val totalPrice = decimal("TOTAL_PRICE", precision = 5, scale = 2).nullable()
    val expirationTime = datetime("EXPIRATION_TIME").nullable()
    val status = enumerationByName<BookingStatus>("STATUS", length = 16)

    // TODO: Tickets?

    context(Transaction)
    fun findBooking(bookingId: BookingId): Booking? =
        BookingTable.selectAll()
            .where { id eq bookingId.value }
            .firstOrNull()
            ?.let { row ->
                when (row[status]) {
                    BookingStatus.UNCONFIRMED ->
                        UnconfirmedBooking(
                            id = BookingId(bookingId.value),
                            userInfo =
                                UserInfo(
                                    name = UserName(row[userName]),
                                    surname = UserSurname(row[userSurname]),
                                ),
                            tickets =
                                nonEmptyListOf(
                                    Ticket(TicketType.CHILD, SeatPlacement(SeatRow(1), SeatNumber(1)))
                                ), // TODO
                            screening =
                                ScreeningTable.findScreening(ScreeningId(row[screeningId].value))
                                    ?: error("Screening does not exist"),
                            bookingTime = BookingTime(row[bookingTime]),
                            totalPrice =
                                row[totalPrice]?.let { BookingPrice(it) }
                                    ?: error("Unconfirmed booking has no price"),
                            expirationTime =
                                row[expirationTime]?.let { BookingExpirationTime(it) }
                                    ?: error("Unconfirmed booking has no expiration time"),
                        )

                    BookingStatus.CONFIRMED ->
                        ConfirmedBooking(
                            id = BookingId(bookingId.value),
                            userInfo =
                                UserInfo(
                                    name = UserName(row[userName]),
                                    surname = UserSurname(row[userSurname]),
                                ),
                            tickets =
                                nonEmptyListOf(
                                    Ticket(TicketType.CHILD, SeatPlacement(SeatRow(1), SeatNumber(1)))
                                ), // TODO
                            screening =
                                ScreeningTable.findScreening(ScreeningId(row[screeningId].value))
                                    ?: error("Screening does not exist, ${row[screeningId].value}"),
                            bookingTime = BookingTime(row[bookingTime]),
                            totalPrice =
                                row[totalPrice]?.let { BookingPrice(it) }
                                    ?: error("Confirmed booking has no price $bookingId"),
                        )

                    BookingStatus.CANCELLED ->
                        CancelledBooking(
                            id = BookingId(bookingId.value),
                            userInfo =
                                UserInfo(
                                    name = UserName(row[userName]),
                                    surname = UserSurname(row[userSurname]),
                                ),
                            tickets =
                                nonEmptyListOf(
                                    Ticket(TicketType.CHILD, SeatPlacement(SeatRow(1), SeatNumber(1)))
                                ), // TODO
                            screening =
                                ScreeningTable.findScreening(ScreeningId(row[screeningId].value))
                                    ?: error("Screening does not exist"),
                            bookingTime = BookingTime(row[bookingTime]),
                            expirationTime =
                                row[expirationTime]?.let { BookingExpirationTime(it) }
                                    ?: error("Unconfirmed booking has no expiration time"),
                            )
                }
            }

    fun delete(bookingId: BookingId) {
        deleteWhere { id eq bookingId.value }
    }

    fun insert(booking: Booking) {
        insert {
            it[userName] = booking.userInfo.name.value
            it[userSurname] = booking.userInfo.surname.value
            it[screeningId] = booking.screening.id.value

            when (booking) {
                is UnconfirmedBooking ->
                    insert {
                        it[totalPrice] = booking.totalPrice.value
                        it[expirationTime] = booking.expirationTime.value
                        it[status] = BookingStatus.UNCONFIRMED
                    }

                is CancelledBooking ->
                    insert {
                        it[totalPrice] = null
                        it[expirationTime] = booking.expirationTime.value
                        it[status] = BookingStatus.CANCELLED
                    }

                is ConfirmedBooking ->
                    insert {
                        it[totalPrice] = booking.totalPrice.value
                        it[expirationTime] = null
                        it[status] = BookingStatus.CONFIRMED
                    }
            }
        }
    }

    enum class BookingStatus {
        UNCONFIRMED,
        CONFIRMED,
        CANCELLED,
    }
}
