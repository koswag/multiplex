package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserInfo
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserName
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserSurname

data class PersistentUserInfo(
    val userName: String,
    val userSurname: String,
)

fun PersistentUserInfo.toDomain() =
    UserInfo(
        name = UserName(userName),
        surname = UserSurname(userSurname),
    )

fun UserInfo.toPersistentUserInfo() =
    PersistentUserInfo(
        userName = name.value,
        userSurname = surname.value,
    )
