package pl.kskarzynski.multiplex.common.util.extensions

fun String.secondPartIsCapitalized(): Boolean =
    hyphenCount() == 0
        || split('-')[1].isCapitalized()

fun String.hyphenCount(): Int =
    count { it == '-' }

fun String.isCapitalized(): Boolean =
    firstOrNull()
        ?.isUpperCase() == true
