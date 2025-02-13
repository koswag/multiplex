package pl.kskarzynski.multiplex.common.infra.json.serializer

import pl.kskarzynski.multiplex.shared.screening.ScreeningId

object ScreeningIdSerializer : IdSerializer<ScreeningId>(::ScreeningId)
