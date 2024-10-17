package pl.kskarzynski.multiplex.screenings.infra.adapter.data.table.model

import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserInfo
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserName
import pl.kskarzynski.multiplex.screenings.domain.model.booking.user.UserSurname

data class PersistentUserInfo(
    val userName: String,
    val userSurname: String,
) {
    fun toDomain() =
        UserInfo(
            name = UserName(userName),
            surname = UserSurname(userSurname),
        )

    companion object {
        fun from(userInfo: UserInfo) =
            PersistentUserInfo(
                userName = userInfo.name.value,
                userSurname = userInfo.surname.value,
            )
    }
}
