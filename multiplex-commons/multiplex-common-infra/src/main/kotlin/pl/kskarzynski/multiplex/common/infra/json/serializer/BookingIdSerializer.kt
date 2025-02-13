package pl.kskarzynski.multiplex.common.infra.json.serializer

import pl.kskarzynski.multiplex.shared.booking.BookingId

object BookingIdSerializer : IdSerializer<BookingId>(::BookingId)
