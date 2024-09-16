package pl.kskarzynski.multiplex.infrastructure.model

import org.jetbrains.exposed.dao.id.UUIDTable

object BookingTable : UUIDTable("BOOKINGS") {
    val userName = varchar("USER_NAME", length = 64)
    val userSurname = varchar("USER_SURNAME", length = 64)
    val screeningId = reference("SCREENING_ID", ScreeningTable)
}
