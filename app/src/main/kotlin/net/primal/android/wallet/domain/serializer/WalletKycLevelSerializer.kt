package net.primal.android.wallet.domain.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.primal.android.wallet.domain.WalletKycLevel

object WalletKycLevelSerializer : KSerializer<WalletKycLevel> {

    override val descriptor = PrimitiveSerialDescriptor("WalletKycLevelSerializer", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: WalletKycLevel) {
        encoder.encodeInt(value.id)
    }

    override fun deserialize(decoder: Decoder): WalletKycLevel {
        return when (val value = decoder.decodeInt()) {
            0 -> WalletKycLevel.None
            1 -> WalletKycLevel.Email
            2 -> WalletKycLevel.Email
            else -> throw IllegalArgumentException("Invalid WalletKycLevel value: $value")
        }
    }
}
