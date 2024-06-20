package net.primal.android.nostr.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import net.primal.android.crypto.bechToBytesOrThrow
import net.primal.android.crypto.toHex

object Nip19TLV {
    enum class Type(val id: Byte) {
        SPECIAL(0),
        RELAY(1),
        AUTHOR(2),
        KIND(3),
    }

    fun toInt32(bytes: ByteArray): Int {
        require(bytes.size == 4) { "length must be 4, got: ${bytes.size}" }
        return ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.BIG_ENDIAN).int
    }

    @Throws(IllegalArgumentException::class)
    fun parse(data: String) = parse(data.bechToBytesOrThrow())

    fun parse(data: ByteArray): Map<Byte, List<ByteArray>> {
        val result = mutableMapOf<Byte, MutableList<ByteArray>>()
        var rest = data
        while (rest.isNotEmpty()) {
            val t = rest[0]
            val l = rest[1].toUByte().toInt()
            val v = rest.sliceArray(IntRange(2, (2 + l) - 1))
            rest = rest.sliceArray(IntRange(2 + l, rest.size - 1))
            if (v.size < l) continue

            if (!result.containsKey(t)) {
                result[t] = mutableListOf()
            }
            result[t]?.add(v)
        }
        return result
    }

    fun parseAsNaddr(naddr: String): Naddr? {
        val tlv = parse(naddr)
        val identifier = tlv[Type.SPECIAL.id]?.first()?.let {
            String(bytes = it, charset = Charsets.US_ASCII)
        }
        val relays = tlv[Type.RELAY.id]?.first()?.let {
            String(bytes = it, charset = Charsets.US_ASCII)
        }
        val profileId = tlv[Type.AUTHOR.id]?.first()?.toHex()

        val kind = tlv[Type.KIND.id]?.first()?.let {
            toInt32(it)
        }

        return if (identifier != null && profileId != null && kind != null) {
            Naddr(
                identifier = identifier,
                relays = relays?.split(",") ?: emptyList(),
                userId = profileId,
                kind = kind,
            )
        } else {
            null
        }
    }
}
