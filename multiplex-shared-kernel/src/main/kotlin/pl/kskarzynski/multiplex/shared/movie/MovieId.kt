@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.shared.movie

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JvmInline
value class MovieId(val value: Uuid) {

    override fun toString() = value.toString()

    companion object {
        fun generate() = MovieId(Uuid.random())
    }
}
