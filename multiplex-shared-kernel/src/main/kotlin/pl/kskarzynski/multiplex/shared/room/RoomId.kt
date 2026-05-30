@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.shared.room

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
value class RoomId(val value: Uuid) {

    override fun toString() = value.toString()

    companion object {
        fun generate() = RoomId(Uuid.random())
    }
}
