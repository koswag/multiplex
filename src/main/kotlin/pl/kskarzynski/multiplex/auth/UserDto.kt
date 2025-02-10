package pl.kskarzynski.multiplex.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val username: String,
    val password: String,
)
