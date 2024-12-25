package pl.kskarzynski.multiplex.rooms.service.rest.dto

import kotlinx.serialization.Serializable

@Serializable
data class SeatDto(
    val row: Int,
    val number: Int,
)
