@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.common.infra.json.serializer

import pl.kskarzynski.multiplex.shared.movie.MovieId
import kotlin.uuid.ExperimentalUuidApi

object MovieIdSerializer : IdSerializer<MovieId>(::MovieId)
