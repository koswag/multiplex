package pl.kskarzynski.multiplex.api.model

import java.util.UUID

data class BookingDto(
    val userInfo: UserInfoDto,
    val tickets: List<TicketDto>,
    val screeningId: UUID,
)
