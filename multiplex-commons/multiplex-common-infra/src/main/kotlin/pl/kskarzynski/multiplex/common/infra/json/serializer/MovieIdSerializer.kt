package pl.kskarzynski.multiplex.common.infra.json.serializer

import pl.kskarzynski.multiplex.shared.movie.MovieId

object MovieIdSerializer : IdSerializer<MovieId>(::MovieId)
