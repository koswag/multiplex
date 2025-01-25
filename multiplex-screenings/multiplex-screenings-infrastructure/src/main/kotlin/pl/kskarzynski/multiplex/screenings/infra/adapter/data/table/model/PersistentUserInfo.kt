package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserInfo
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserName
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserSurname

internal data class PersistentUserInfo(
    val userName: String,
    val userSurname: String,
)

internal fun PersistentUserInfo.toDomain() =
    UserInfo(
        name = UserName(userName),
        surname = UserSurname(userSurname),
    )

internal fun UserInfo.toPersistentUserInfo() =
    PersistentUserInfo(
        userName = name.value,
        userSurname = surname.value,
    )
