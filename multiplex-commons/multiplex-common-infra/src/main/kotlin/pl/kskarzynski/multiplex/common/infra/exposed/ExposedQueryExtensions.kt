package pl.kskarzynski.multiplex.common.infra.exposed

import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import pl.kskarzynski.multiplex.shared.misc.Page
import pl.kskarzynski.multiplex.shared.misc.PagingRequest

inline fun <T> Query.page(paging: PagingRequest, transform: (ResultRow) -> T): Page<T> {
    val totalCount = this.count()
    val totalPages = (totalCount + paging.pageSize - 1) / paging.pageSize

    val pagedQuery = this.limit(paging.pageSize, paging.offset.toLong())

    return Page(
        content = pagedQuery.map(transform),
        pageNumber = paging.pageNumber,
        pageSize = paging.pageSize,
        totalPages = totalPages.toInt(),
    )
}
