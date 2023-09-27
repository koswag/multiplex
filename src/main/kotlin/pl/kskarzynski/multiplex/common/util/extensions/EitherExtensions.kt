package pl.kskarzynski.multiplex.common.util.extensions

import arrow.core.EitherNel

fun <E, EE, A> EitherNel<E, A>.mapErrors(transform: (E) -> EE): EitherNel<EE, A> =
    mapLeft { errors ->
        errors.map(transform)
    }
