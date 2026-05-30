@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.shared.booking

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
value class BookingId(val value: Uuid) {

    override fun toString() = value.toString()

    companion object {
        fun generate() = BookingId(Uuid.random())
    }
}
