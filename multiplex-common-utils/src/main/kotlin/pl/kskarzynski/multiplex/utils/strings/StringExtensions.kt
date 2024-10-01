package pl.kskarzynski.multiplex.utils.strings

fun String.secondPartIsCapitalized(): Boolean {
    val parts = this.split("-")

    return parts.size == 1
        || parts[1].isCapitalized()
}

fun String.hyphenCount(): Int =
    count { it == '-' }

fun String.isCapitalized(): Boolean {
    if (isEmpty()) return false

    val firstCharacter = this.first()
    val rest = this.drop(1).takeWhile { it != '-' }

    return firstCharacter.isUpperCase()
        && rest.all { it.isLowerCase() }
}
