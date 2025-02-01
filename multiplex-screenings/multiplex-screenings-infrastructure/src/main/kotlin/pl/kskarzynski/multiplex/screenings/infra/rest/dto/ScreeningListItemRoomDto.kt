@file:UseSerializers(UuidSerializer::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer

@Serializable
data class ScreeningListItemRoomDto(
    val id: UUID,
    val number: Int,
)
