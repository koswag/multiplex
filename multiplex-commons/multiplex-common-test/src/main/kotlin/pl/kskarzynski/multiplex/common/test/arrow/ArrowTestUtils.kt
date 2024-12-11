package pl.kskarzynski.multiplex.common.test.arrow

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import strikt.api.Assertion
import strikt.assertions.isA

inline fun <reified L, R> Assertion.Builder<Either<L, R>>.isLeft(): Assertion.Builder<L> =
    this.isA<Left<L>>()
        .get { value }

inline fun <L, reified R> Assertion.Builder<Either<L, R>>.isRight(): Assertion.Builder<R> =
    this.isA<Right<R>>()
        .get { value }
