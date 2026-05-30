@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.common.infra.json.serializer

import pl.kskarzynski.multiplex.shared.screening.ScreeningId
import kotlin.uuid.ExperimentalUuidApi

object ScreeningIdSerializer : IdSerializer<ScreeningId>(::ScreeningId)
