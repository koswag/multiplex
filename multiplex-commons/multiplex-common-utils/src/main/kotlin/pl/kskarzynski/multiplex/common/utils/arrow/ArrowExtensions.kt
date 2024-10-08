package pl.kskarzynski.multiplex.common.utils.arrow

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.RaiseDSL
import arrow.core.raise.mapOrAccumulate
import arrow.core.toNonEmptyListOrNull

@RaiseDSL
fun <E> Raise<NonEmptyList<E>>.accumulateErrors(
    vararg checks: Raise<E>.() -> Unit,
) {
    mapOrAccumulate(checks.asIterable()) { check -> check() }
}

fun <T> List<T>.toNonEmptyList(): NonEmptyList<T> =
    this.toNonEmptyListOrNull()
        ?: throw IllegalArgumentException("List is empty")
