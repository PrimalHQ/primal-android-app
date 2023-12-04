package net.primal.android.wallet.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object WalletKycLevelSerializer : KSerializer<WalletKycLevel> {

    override val descriptor = PrimitiveSerialDescriptor("WalletKycLevelSerializer", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: WalletKycLevel) {
        encoder.encodeInt(value.ordinal)
    }

    override fun deserialize(decoder: Decoder): WalletKycLevel {
        return when (val value = decoder.decodeInt()) {
            0 -> WalletKycLevel.None
            1 -> WalletKycLevel.Email
            2 -> WalletKycLevel.IdDocument
            else -> throw IllegalArgumentException("Invalid WalletKycLevel value: $value")
        }
    }
}
