package pl.kskarzynski.multiplex.domain.user.model

@JvmInline
value class UserName(val value: String)

@JvmInline
value class UserSurname(val value: String)

data class UserInfo(
    val name: UserName,
    val surname: UserSurname,
)
