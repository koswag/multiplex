package pl.kskarzynski.multiplex.common.infra.misc

import kotlinx.serialization.Serializable
import pl.kskarzynski.multiplex.shared.misc.Page

@Serializable
data class PageDto<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalPages: Int,
)

fun <T> Page<T>.toDto() =
    PageDto(
        content = content,
        pageNumber = pageNumber,
        pageSize = pageSize,
        totalPages = totalPages,
    )
