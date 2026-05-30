@file:OptIn(ExperimentalUuidApi::class)

package pl.kskarzynski.multiplex.common.infra.json.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

abstract class IdSerializer<ID>(
    private val createInstance: (Uuid) -> ID,
) : KSerializer<ID> {

    override val descriptor = PrimitiveSerialDescriptor(getSerialName(), PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ID =
        createInstance(Uuid.parse(decoder.decodeString()))

    override fun serialize(encoder: Encoder, value: ID) {
        encoder.encodeString(value.toString())
    }
}

private fun IdSerializer<*>.getSerialName(): String =
    this::class.simpleName!!.removeSuffix("Serializer")
