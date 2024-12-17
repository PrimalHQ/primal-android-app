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

    private fun String.cleanNostrScheme(): String {
        return if (this.startsWith("nostr:")) {
            this.removePrefix("nostr:")
        } else {
            this
        }
    }

    fun parseUriAsNeventOrNull(neventUri: String): Nevent? =
        runCatching {
            parseAsNevent(nevent = neventUri.cleanNostrScheme())
        }.getOrNull()

    private fun parseAsNevent(nevent: String): Nevent? {
        val tlv = parse(nevent)
        val eventId = tlv[Type.SPECIAL.id]?.first()?.toHex()

        val relays = tlv[Type.RELAY.id]?.first()?.let {
            String(bytes = it, charset = Charsets.US_ASCII)
        }
        val profileId = tlv[Type.AUTHOR.id]?.first()?.toHex()

        val kind = tlv[Type.KIND.id]?.first()?.let {
            toInt32(it)
        }
        return if (eventId != null && profileId != null && kind != null) {
            Nevent(
                kind = kind,
                eventId = eventId,
                userId = profileId,
                relays = relays?.split(",") ?: emptyList(),
            )
        } else {
            null
        }
    }

    fun parseUriAsNaddrOrNull(naddrUri: String) =
        runCatching {
            parseAsNaddrOrNull(naddr = naddrUri.cleanNostrScheme())
        }.getOrNull()

    private fun parseAsNaddrOrNull(naddr: String): Naddr? {
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

    fun Nevent.asNeventString(): String {
        val tlv = mutableListOf<Byte>()

        this.eventId.addIdentifierBytes(tlv::addAll)

//        if (relays.isNotEmpty()) {
//            this.relays.addRelayBytes(tlv::addAll)
//        }

//        this.userId.addAuthorBytes(tlv::addAll)

//        this.kind.toKindBytes(tlv::addAll)

        return Bech32.encodeBytes(
            hrp = "nevent",
            data = tlv.toByteArray(),
            encoding = Bech32.Encoding.Bech32,
        )
    }

    fun Naddr.toNaddrString(): String {
        val tlv = mutableListOf<Byte>()

        // Add SPECIAL type
        this.identifier.addIdentifierBytes(tlv::addAll)

        // Add RELAY type if not empty
        if (this.relays.isNotEmpty()) {
            this.relays.addRelayBytes(tlv::addAll)
        }

        // Add AUTHOR type
        this.userId.addAuthorBytes(tlv::addAll)

        // Add KIND type
        this.kind.toKindBytes(tlv::addAll)

        return Bech32.encodeBytes(
            hrp = "naddr",
            data = tlv.toByteArray(),
            encoding = Bech32.Encoding.Bech32,
        )
    }

    private fun Int.toKindBytes(addAll: (List<Byte>) -> Boolean) {
        val kindBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(this).array()
        addAll(kindBytes.toTLVBytes(type = Type.KIND))
    }

    private fun String.addAuthorBytes(addAll: (List<Byte>) -> Boolean) {
        val authorBytes = this.hexToBytes()
        addAll(authorBytes.toTLVBytes(type = Type.AUTHOR))
    }

    private fun String.addIdentifierBytes(addAll: (List<Byte>) -> Boolean) {
        val identifierBytes = this.toByteArray(Charsets.US_ASCII)
        addAll(identifierBytes.toTLVBytes(type = Type.SPECIAL))
    }

    private fun List<String>.addRelayBytes(addAll: (List<Byte>) -> Boolean) {
        val relaysBytes = this.joinToString(",").toByteArray(Charsets.US_ASCII)
        addAll(relaysBytes.toTLVBytes(type = Type.RELAY))
    }

    private fun ByteArray.toTLVBytes(type: Type) =
        listOf(
            type.id,
            this.size.toByte(),
        ) + this.toList()


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
