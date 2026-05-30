@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.shared.screening

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@JvmInline
value class ScreeningId(val value: Uuid) {

    override fun toString() = value.toString()

    companion object {
        fun generate() = ScreeningId(Uuid.random())
    }
}
