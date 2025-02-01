package pl.kskarzynski.multiplex.screenings.domain.model.view

import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

data class ScreeningListItemView(
    val id: ScreeningId,
    val startTime: ScreeningStartTime,
    val room: ScreeningListItemRoomView,
)
