package pl.kskarzynski.multiplex.shared.misc

data class PagingRequest(
    val pageNumber: Int,
    val pageSize: Int,
) {
    val offset: Int
        get() = (pageNumber - 1) * pageSize
}

