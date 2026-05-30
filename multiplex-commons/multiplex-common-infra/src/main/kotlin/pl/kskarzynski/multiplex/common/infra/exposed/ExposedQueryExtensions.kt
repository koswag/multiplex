package pl.kskarzynski.multiplex.common.infra.exposed

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.r2dbc.Query
import pl.kskarzynski.multiplex.shared.misc.Page
import pl.kskarzynski.multiplex.shared.misc.PagingRequest

suspend inline fun <T> Query.page(paging: PagingRequest, crossinline transform: (ResultRow) -> T): Page<T> {
    val pagedQuery = this
        .limit(paging.pageSize)
        .offset(paging.offset.toLong())
    return Page(
        content = pagedQuery.map(transform).toList(),
        pageNumber = paging.pageNumber,
        pageSize = paging.pageSize,
        totalCount = this.count(),
    )
}
