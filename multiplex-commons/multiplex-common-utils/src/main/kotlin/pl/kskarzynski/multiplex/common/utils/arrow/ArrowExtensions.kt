package pl.kskarzynski.multiplex.common.utils.arrow

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.RaiseDSL
import arrow.core.raise.mapOrAccumulate
import arrow.core.toNonEmptyListOrNull

@RaiseDSL
fun <E> Raise<NonEmptyList<E>>.accumulateErrors(vararg checks: Raise<E>.() -> Unit) {
    mapOrAccumulate(checks.asIterable()) { check -> check() }
}

@RaiseDSL
fun <E, T> Raise<NonEmptyList<E>>.accumulateErrors(items: Iterable<T>, check: Raise<E>.(T) -> Unit) {
    mapOrAccumulate(items, check)
}

fun <T> Collection<T>.toNonEmptyList(): NonEmptyList<T> =
    this.toNonEmptyListOrNull()
        ?: throw IllegalArgumentException("List is empty")

fun <L, R> Either<NonEmptyList<NonEmptyList<L>> ,R>.flattenErrors(): Either<NonEmptyList<L>, R> =
    mapLeft { it.flatten() }

fun <T> NonEmptyList<NonEmptyList<T>>.flatten(): NonEmptyList<T> = flatMap { it }
