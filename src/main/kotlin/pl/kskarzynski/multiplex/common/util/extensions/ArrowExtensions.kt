package pl.kskarzynski.multiplex.common.util.extensions

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.RaiseDSL
import arrow.core.raise.mapOrAccumulate

@RaiseDSL
fun <E> Raise<NonEmptyList<E>>.accumulateErrors(
    vararg checks: Raise<E>.() -> Unit,
) {
    mapOrAccumulate(checks.asIterable()) { check -> check() }
}
