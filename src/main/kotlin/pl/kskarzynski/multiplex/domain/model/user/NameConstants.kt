package pl.kskarzynski.multiplex.domain.model.user

val LOWERCASE_CHARACTERS = ('a'..'z').toSet()
val UPPERCASE_CHARACTERS = ('A'..'Z').toSet()

val POLISH_CHARACTERS = setOf(
    Char(261), Char(260), Char(263),
    Char(262), Char(281), Char(280),
    Char(322), Char(321), Char(324),
    Char(323), Char(243), Char(211),
    Char(347), Char(346), Char(378),
    Char(377), Char(380), Char(379),
)

const val HYPHEN = '-'
