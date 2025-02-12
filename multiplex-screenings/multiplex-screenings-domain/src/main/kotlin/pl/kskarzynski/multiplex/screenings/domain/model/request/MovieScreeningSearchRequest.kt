package pl.kskarzynski.multiplex.screenings.domain.model.request

import pl.kskarzynski.multiplex.shared.misc.PagingRequest
import pl.kskarzynski.multiplex.shared.movie.MovieId
import pl.kskarzynski.multiplex.shared.screening.ScreeningStartTime

data class MovieScreeningSearchRequest(
    val movieId: MovieId,
    val pagingRequest: PagingRequest,
    val minStartTime: ScreeningStartTime?,
    val maxStartTime: ScreeningStartTime?,
)