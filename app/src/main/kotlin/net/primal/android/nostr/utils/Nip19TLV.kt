package net.primal.android.nostr.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import net.primal.android.crypto.Bech32
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

    fun parseAsNaddr(naddr: String): Naddr? =
        runCatching {
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
            if (identifier != null && profileId != null && kind != null) {
                Naddr(
                    identifier = identifier,
                    relays = relays?.split(",") ?: emptyList(),
                    userId = profileId,
                    kind = kind,
                )
            } else {
                null
            }
        }.getOrNull()

    fun Naddr.toNaddrString(): String {
        val tlv = mutableListOf<Byte>()

        // Add SPECIAL type
        val identifierBytes = this.identifier.toByteArray(Charsets.US_ASCII)
        tlv.add(Type.SPECIAL.id)
        tlv.add(identifierBytes.size.toByte())
        tlv.addAll(identifierBytes.toList())

        // Add RELAY type if not empty
        if (this.relays.isNotEmpty()) {
            val relaysBytes = this.relays.joinToString(",").toByteArray(Charsets.US_ASCII)
            tlv.add(Type.RELAY.id)
            tlv.add(relaysBytes.size.toByte())
            tlv.addAll(relaysBytes.toList())
        }

        // Add AUTHOR type
        val authorBytes = this.userId.hexToBytes()
        tlv.add(Type.AUTHOR.id)
        tlv.add(authorBytes.size.toByte())
        tlv.addAll(authorBytes.toList())

        // Add KIND type
        val kindBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(this.kind).array()
        tlv.add(Type.KIND.id)
        tlv.add(kindBytes.size.toByte())
        tlv.addAll(kindBytes.toList())

        return Bech32.encodeBytes(
            hrp = "naddr",
            data = tlv.toByteArray(),
            encoding = Bech32.Encoding.Bech32,
        )
    }

    @Suppress("MagicNumber")
    private fun String.hexToBytes(): ByteArray {
        val cleanedInput = this.replace(Regex("[^0-9A-Fa-f]"), "")
        val len = cleanedInput.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = (
                (Character.digit(cleanedInput[i], 16) shl 4) +
                    Character.digit(cleanedInput[i + 1], 16)
                ).toByte()
        }
        return data
    }
}
