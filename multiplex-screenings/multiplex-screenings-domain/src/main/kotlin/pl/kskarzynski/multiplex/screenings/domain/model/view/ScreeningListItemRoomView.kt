package pl.kskarzynski.multiplex.screenings.domain.model.view

import pl.kskarzynski.multiplex.shared.room.RoomId
import pl.kskarzynski.multiplex.shared.room.RoomNumber

data class ScreeningListItemRoomView(
    val id: RoomId,
    val number: RoomNumber,
)
