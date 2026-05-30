@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.common.infra.json.serializer

import pl.kskarzynski.multiplex.shared.room.RoomId
import kotlin.uuid.ExperimentalUuidApi

object RoomIdSerializer : IdSerializer<RoomId>(::RoomId)
