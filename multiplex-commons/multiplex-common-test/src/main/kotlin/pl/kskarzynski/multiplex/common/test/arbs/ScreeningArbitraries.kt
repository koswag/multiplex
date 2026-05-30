@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.common.test.arbs

import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.uuid
import pl.kskarzynski.multiplex.shared.booking.BookingId
import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

fun Arb.Companion.bookingId(): Arb<BookingId> =
    Arb.uuid().map { BookingId(it.toKotlinUuid()) }

fun Arb.Companion.screeningId(): Arb<ScreeningId> =
    Arb.uuid().map { ScreeningId(it.toKotlinUuid()) }
