package pl.kskarzynski.multiplex.shared.misc

data class Page<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalPages: Int,
)

fun <T, R> Page<T>.map(func: (T) -> R): Page<R> =
    Page(
        content = content.map(func),
        pageNumber = pageNumber,
        pageSize = pageSize,
        totalPages = totalPages,
    )
