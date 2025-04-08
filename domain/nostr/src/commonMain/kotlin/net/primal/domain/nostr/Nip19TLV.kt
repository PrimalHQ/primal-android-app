package net.primal.domain.nostr

import fr.acinq.bitcoin.Bech32
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.core.writeFully
import kotlinx.io.readByteArray
import kotlinx.io.readString
import net.primal.domain.nostr.cryptography.utils.bechToBytesOrThrow
import net.primal.domain.nostr.cryptography.utils.toHex

object Nip19TLV {
    enum class Type(val id: Byte) {
        SPECIAL(0),
        RELAY(1),
        AUTHOR(2),
        KIND(3),
    }

    fun toInt32(bytes: ByteArray): Int {
        require(bytes.size == 4) { "length must be 4, got: ${bytes.size}" }
        val packet = buildPacket {
            writeFully(bytes)
        }
        return packet.readInt()
    }

    fun ByteArray.readAsString(): String =
        buildPacket {
            writeFully(this@readAsString)
        }.readString()

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

    private fun String.cleanNostrScheme(): String = this.removePrefix("nostr:")

    fun parseUriAsNprofileOrNull(nprofileUri: String): Nprofile? =
        runCatching {
            parseAsNprofile(nprofile = nprofileUri.cleanNostrScheme())
        }.getOrNull()

    private fun parseAsNprofile(nprofile: String): Nprofile? {
        val tlv = parse(nprofile)
        val pubkey = tlv[Type.SPECIAL.id]?.first()?.toHex()

        val relays = tlv[Type.RELAY.id]?.map { it.readAsString() } ?: emptyList()

        return pubkey?.let {
            Nprofile(
                pubkey = pubkey,
                relays = relays,
            )
        }
    }

    fun parseUriAsNeventOrNull(neventUri: String): Nevent? =
        runCatching {
            parseAsNevent(nevent = neventUri.cleanNostrScheme())
        }.getOrNull()

    private fun parseAsNevent(nevent: String): Nevent? {
        val tlv = parse(nevent)
        val eventId = tlv[Type.SPECIAL.id]?.first()?.toHex()

        val relays = tlv[Type.RELAY.id]?.map { it.readAsString() } ?: emptyList()

        val profileId = tlv[Type.AUTHOR.id]?.firstOrNull()?.toHex()

        val kind = tlv[Type.KIND.id]?.firstOrNull()?.let {
            toInt32(it)
        }

        return eventId?.let {
            Nevent(
                kind = kind,
                eventId = eventId,
                userId = profileId,
                relays = relays,
            )
        }
    }

    fun parseUriAsNaddrOrNull(naddrUri: String) =
        runCatching {
            parseAsNaddrOrNull(naddr = naddrUri.cleanNostrScheme())
        }.getOrNull()

    private fun parseAsNaddrOrNull(naddr: String): Naddr? {
        val tlv = parse(naddr)
        val identifier = tlv[Type.SPECIAL.id]?.first()?.readAsString()

        val relays = tlv[Type.RELAY.id]?.map { it.readAsString() } ?: emptyList()

        val profileId = tlv[Type.AUTHOR.id]?.first()?.toHex()

        val kind = tlv[Type.KIND.id]?.first()?.let {
            toInt32(it)
        }

        return if (identifier != null && profileId != null && kind != null) {
            Naddr(
                identifier = identifier,
                relays = relays,
                userId = profileId,
                kind = kind,
            )
        } else {
            null
        }
    }

    fun Nprofile.toNprofileString(): String {
        val tlv = mutableListOf<Byte>()

        // Add profile public key
        tlv.addAll(this.pubkey.constructNprofileSpecialBytes())

        // Add RELAY type if not empty
        if (this.relays.isNotEmpty()) {
            tlv.addAll(this.relays.constructRelayBytes())
        }

        return Bech32.encodeBytes(
            hrp = "nprofile",
            data = tlv.toByteArray(),
            encoding = Bech32.Encoding.Bech32,
        )
    }

    fun Nevent.toNeventString(): String {
        val tlv = mutableListOf<Byte>()

        // Add EVENT_ID type
        tlv.addAll(this.eventId.constructNeventSpecialBytes())

        // Add RELAY type if not empty
        if (this.relays.isNotEmpty()) {
            tlv.addAll(this.relays.constructRelayBytes())
        }

        // Add AUTHOR type
        this.userId?.let { tlv.addAll(this.userId.constructAuthorBytes()) }

        // Add KIND type
        if (this.kind != null) {
            tlv.addAll(this.kind.constructKindBytes())
        }

        return Bech32.encodeBytes(
            hrp = "nevent",
            data = tlv.toByteArray(),
            encoding = Bech32.Encoding.Bech32,
        )
    }

    fun Naddr.toNaddrString(): String {
        val tlv = mutableListOf<Byte>()

        // Add SPECIAL type
        tlv.addAll(this.identifier.constructNaddrIdentifierBytes())

        // Add RELAY type if not empty
        if (this.relays.isNotEmpty()) {
            tlv.addAll(this.relays.constructRelayBytes())
        }

        // Add AUTHOR type
        tlv.addAll(this.userId.constructAuthorBytes())

        // Add KIND type
        tlv.addAll(this.kind.constructKindBytes())

        return Bech32.encodeBytes(
            hrp = "naddr",
            data = tlv.toByteArray(),
            encoding = Bech32.Encoding.Bech32,
        )
    }

    private fun Int.constructKindBytes(): List<Byte> {
        val kindBytes = buildPacket {
            writeInt(this@constructKindBytes)
        }.readByteArray()
        return kindBytes.toTLVBytes(type = Type.KIND)
    }

    private fun String.constructNprofileSpecialBytes(): List<Byte> {
        val authorBytes = this.hexToBytes()
        return authorBytes.toTLVBytes(type = Type.SPECIAL)
    }

    private fun String.constructNeventSpecialBytes(): List<Byte> {
        val authorBytes = this.hexToBytes()
        return authorBytes.toTLVBytes(type = Type.SPECIAL)
    }

    private fun String.constructAuthorBytes(): List<Byte> {
        val authorBytes = this.hexToBytes()
        return authorBytes.toTLVBytes(type = Type.AUTHOR)
    }

    private fun String.constructNaddrIdentifierBytes(): List<Byte> {
        val identifierBytes = this.toByteArray(charset = Charsets.ISO_8859_1)
        return identifierBytes.toTLVBytes(type = Type.SPECIAL)
    }

    private fun List<String>.constructRelayBytes(): List<Byte> {
        return flatMap { it.toByteArray(charset = Charsets.ISO_8859_1).toTLVBytes(type = Type.RELAY) }
    }

    private fun ByteArray.toTLVBytes(type: Type) =
        listOf(
            type.id,
            this.size.toByte(),
        ) + this.toList()

    @Suppress("MagicNumber")
    fun String.hexToBytes(): ByteArray {
        val cleanedInput = this.replace(Regex("[^0-9A-Fa-f]"), "")
        require(cleanedInput.length % 2 == 0) { "Hex string must have an even number of characters." }
        val data = ByteArray(cleanedInput.length / 2)
        for (i in cleanedInput.indices step 2) {
            val high = hexCharToInt(cleanedInput[i])
            val low = hexCharToInt(cleanedInput[i + 1])
            data[i / 2] = ((high shl 4) + low).toByte()
        }
        return data
    }

    private fun hexCharToInt(c: Char): Int =
        when (c) {
            in '0'..'9' -> c - '0'
            in 'A'..'F' -> c - 'A' + 10
            in 'a'..'f' -> c - 'a' + 10
            else -> throw IllegalArgumentException("Invalid hex character: $c")
        }
}
