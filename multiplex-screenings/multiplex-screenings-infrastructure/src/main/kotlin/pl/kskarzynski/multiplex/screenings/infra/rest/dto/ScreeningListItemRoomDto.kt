@file:UseSerializers(UuidSerializer::class)
@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.screenings.infra.rest.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import pl.kskarzynski.multiplex.common.infra.json.serializer.UuidSerializer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ScreeningListItemRoomDto(
    val id: Uuid,
    val number: Int,
)
