package net.primal.android.wallet.domain.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.primal.android.wallet.domain.SubWallet

object SubWalletSerializer : KSerializer<SubWallet> {

    override val descriptor = PrimitiveSerialDescriptor("SubWalletSerializer", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: SubWallet) {
        encoder.encodeInt(value.id)
    }

    override fun deserialize(decoder: Decoder): SubWallet {
        return when (val value = decoder.decodeInt()) {
            1 -> SubWallet.Open
            else -> throw IllegalArgumentException("Invalid SubWallet value: $value")
        }
    }
}
