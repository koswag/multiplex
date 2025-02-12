package pl.kskarzynski.multiplex.screenings.infra.rest

import pl.kskarzynski.multiplex.common.infra.misc.PageDto
import pl.kskarzynski.multiplex.screenings.infra.rest.dto.ScreeningListItemDto

sealed interface MovieScreeningSearchResult {
    data object MovieNotFound : MovieScreeningSearchResult
    data class Success(val screenings: PageDto<ScreeningListItemDto>) : MovieScreeningSearchResult
}
