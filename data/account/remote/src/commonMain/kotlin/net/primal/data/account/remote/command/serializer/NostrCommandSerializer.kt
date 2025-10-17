package net.primal.data.account.remote.command.serializer

import kotlin.enums.enumEntries
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.primal.data.account.remote.command.model.NostrCommandMethod

object NostrCommandSerializer : KSerializer<NostrCommandMethod> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NostrCommandMethod", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: NostrCommandMethod) {
        encoder.encodeString(value.method)
    }

    override fun deserialize(decoder: Decoder): NostrCommandMethod {
        val decoded = decoder.decodeString()
        return enumEntries<NostrCommandMethod>().firstOrNull { it.method == decoded }
            ?: throw IllegalArgumentException("Unknown method: $decoded")
    }
}
