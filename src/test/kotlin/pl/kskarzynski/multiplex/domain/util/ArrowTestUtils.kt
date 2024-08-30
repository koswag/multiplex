package pl.kskarzynski.multiplex.domain.util

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import io.kotest.matchers.types.shouldBeInstanceOf

inline fun <reified L> Either<L, *>.shouldBeLeft(block: (L) -> Unit): Left<L> =
    this.shouldBeLeft<L>()
        .also { block(it.value) }

inline fun <reified L> Either<L, *>.shouldBeLeft(): Left<L> =
    this.shouldBeInstanceOf<Left<L>>()


inline fun <reified R> Either<*, R>.shouldBeRight(block: (R) -> Unit): Right<R> =
    this.shouldBeRight()
        .also { block(it.value) }

inline fun <reified R> Either<*, R>.shouldBeRight(): Right<R> =
    this.shouldBeInstanceOf<Right<R>>()
