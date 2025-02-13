package pl.kskarzynski.multiplex.common.infra.json.serializer

import pl.kskarzynski.multiplex.shared.room.RoomId

object RoomIdSerializer : IdSerializer<RoomId>(::RoomId)
