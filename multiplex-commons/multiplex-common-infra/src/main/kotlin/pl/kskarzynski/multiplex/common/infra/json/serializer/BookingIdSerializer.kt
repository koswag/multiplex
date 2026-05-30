@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.common.infra.json.serializer

import pl.kskarzynski.multiplex.shared.booking.BookingId
import kotlin.uuid.ExperimentalUuidApi

object BookingIdSerializer : IdSerializer<BookingId>(::BookingId)
