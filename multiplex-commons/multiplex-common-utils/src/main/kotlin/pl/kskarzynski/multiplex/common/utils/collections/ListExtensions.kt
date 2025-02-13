package pl.kskarzynski.multiplex.common.utils.collections

fun <T> List<T>.replace(element: T, replacement: T): List<T> =
    map { if (it == element) replacement else it }
