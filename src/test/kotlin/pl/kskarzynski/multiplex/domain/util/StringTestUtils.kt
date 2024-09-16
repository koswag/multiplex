package pl.kskarzynski.multiplex.domain.util

fun String.insert(offset: Int, char: Char): String =
    buildString {
        append(this@insert)
        insert(offset, char)
    }
