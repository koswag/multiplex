package pl.kskarzynski.multiplex.api.model

data class TicketDto(
    val type: TicketTypeDto,
    val placement: SeatPlacementDto,
)
