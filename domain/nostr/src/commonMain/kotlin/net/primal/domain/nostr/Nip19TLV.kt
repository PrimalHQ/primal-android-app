package net.primal.domain.nostr

// import java.nio.ByteBuffer
// import java.nio.ByteOrder
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import kotlinx.io.readString
import net.primal.domain.nostr.cryptography.bechToBytesOrThrow

// TODO Complete Nip19TLV. Port java.nio to Kotlin native

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

    private fun ByteArray.readAsString(): String =
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

//    fun parseUriAsNprofileOrNull(nprofileUri: String): Nprofile? =
//        runCatching {
//            parseAsNprofile(nprofile = nprofileUri.cleanNostrScheme())
//        }.getOrNull()

//    private fun parseAsNprofile(nprofile: String): Nprofile? {
//        val tlv = parse(nprofile)
//        val pubkey = tlv[Type.SPECIAL.id]?.first()?.toHex()
//
//        val relays = tlv[Type.RELAY.id]?.map {
//            String(bytes = it, charset = Charsets.US_ASCII)
//        } ?: emptyList()
//
//        return pubkey?.let {
//            Nprofile(
//                pubkey = pubkey,
//                relays = relays,
//            )
//        }
//    }

//    fun parseUriAsNeventOrNull(neventUri: String): Nevent? =
//        runCatching {
//            parseAsNevent(nevent = neventUri.cleanNostrScheme())
//        }.getOrNull()

//    private fun parseAsNevent(nevent: String): Nevent? {
//        val tlv = parse(nevent)
//        val eventId = tlv[Type.SPECIAL.id]?.first()?.toHex()
//
//        val relays = tlv[Type.RELAY.id]?.map {
//            String(bytes = it, charset = Charsets.US_ASCII)
//        } ?: emptyList()
//
//        val profileId = tlv[Type.AUTHOR.id]?.firstOrNull()?.toHex()
//
//        val kind = tlv[Type.KIND.id]?.firstOrNull()?.let {
//            toInt32(it)
//        }
//
//        return eventId?.let {
//            Nevent(
//                kind = kind,
//                eventId = eventId,
//                userId = profileId,
//                relays = relays,
//            )
//        }
//    }

//    fun parseUriAsNaddrOrNull(naddrUri: String) =
//        runCatching {
//            parseAsNaddrOrNull(naddr = naddrUri.cleanNostrScheme())
//        }.getOrNull()
//
//    private fun parseAsNaddrOrNull(naddr: String): Naddr? {
//        val tlv = parse(naddr)
//        val identifier = tlv[Type.SPECIAL.id]?.first()?.let {
//            String(bytes = it, charset = Charsets.US_ASCII)
//        }
//        val relays = tlv[Type.RELAY.id]?.map {
//            String(bytes = it, charset = Charsets.US_ASCII)
//        } ?: emptyList()
//
//        val profileId = tlv[Type.AUTHOR.id]?.first()?.toHex()
//
//        val kind = tlv[Type.KIND.id]?.first()?.let {
//            toInt32(it)
//        }
//
//        return if (identifier != null && profileId != null && kind != null) {
//            Naddr(
//                identifier = identifier,
//                relays = relays,
//                userId = profileId,
//                kind = kind,
//            )
//        } else {
//            null
//        }
//    }

//    fun Nprofile.toNprofileString(): String {
//        val tlv = mutableListOf<Byte>()
//
//        // Add profile public key
//        tlv.addAll(this.pubkey.constructNprofileSpecialBytes())
//
//        // Add RELAY type if not empty
//        if (this.relays.isNotEmpty()) {
//            tlv.addAll(this.relays.constructRelayBytes())
//        }
//
//        return Bech32.encodeBytes(
//            hrp = "nprofile",
//            data = tlv.toByteArray(),
//            encoding = Bech32.Encoding.Bech32,
//        )
//    }

//    fun Nevent.toNeventString(): String {
//        val tlv = mutableListOf<Byte>()
//
//        // Add EVENT_ID type
//        tlv.addAll(this.eventId.constructNeventSpecialBytes())
//
//        // Add RELAY type if not empty
//        if (this.relays.isNotEmpty()) {
//            tlv.addAll(this.relays.constructRelayBytes())
//        }
//
//        // Add AUTHOR type
//        this.userId?.let { tlv.addAll(this.userId.constructAuthorBytes()) }
//
//        // Add KIND type
//        if (this.kind != null) {
//            tlv.addAll(this.kind.constructKindBytes())
//        }
//
//        return Bech32.encodeBytes(
//            hrp = "nevent",
//            data = tlv.toByteArray(),
//            encoding = Bech32.Encoding.Bech32,
//        )
//    }

//    fun Naddr.toNaddrString(): String {
//        val tlv = mutableListOf<Byte>()
//
//        // Add SPECIAL type
//        tlv.addAll(this.identifier.constructNaddrIdentifierBytes())
//
//        // Add RELAY type if not empty
//        if (this.relays.isNotEmpty()) {
//            tlv.addAll(this.relays.constructRelayBytes())
//        }
//
//        // Add AUTHOR type
//        tlv.addAll(this.userId.constructAuthorBytes())
//
//        // Add KIND type
//        tlv.addAll(this.kind.constructKindBytes())
//
//        return Bech32.encodeBytes(
//            hrp = "naddr",
//            data = tlv.toByteArray(),
//            encoding = Bech32.Encoding.Bech32,
//        )
//    }

//    private fun Int.constructKindBytes(): List<Byte> {
//        val kindBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(this).array()
//        return kindBytes.toTLVBytes(type = Type.KIND)
//    }

//    private fun String.constructNprofileSpecialBytes(): List<Byte> {
//        val authorBytes = this.hexToBytes()
//        return authorBytes.toTLVBytes(type = Type.SPECIAL)
//    }

//    private fun String.constructNeventSpecialBytes(): List<Byte> {
//        val authorBytes = this.hexToBytes()
//        return authorBytes.toTLVBytes(type = Type.SPECIAL)
//    }

//    private fun String.constructAuthorBytes(): List<Byte> {
//        val authorBytes = this.hexToBytes()
//        return authorBytes.toTLVBytes(type = Type.AUTHOR)
//    }

//    private fun String.constructNaddrIdentifierBytes(): List<Byte> {
//        val identifierBytes = this.toByteArray(Charsets.US_ASCII)
//        return identifierBytes.toTLVBytes(type = Type.SPECIAL)
//    }

//    private fun List<String>.constructRelayBytes(): List<Byte> {
//        return flatMap { it.toByteArray(Charsets.US_ASCII).toTLVBytes(type = Type.RELAY) }
//    }

//    private fun ByteArray.toTLVBytes(type: Type) =
//        listOf(
//            type.id,
//            this.size.toByte(),
//        ) + this.toList()

//    @Suppress("MagicNumber")
//    private fun String.hexToBytes(): ByteArray {
//        val cleanedInput = this.replace(Regex("[^0-9A-Fa-f]"), "")
//        val len = cleanedInput.length
//        val data = ByteArray(len / 2)
//        for (i in 0 until len step 2) {
//            data[i / 2] = (
//                (Character.digit(cleanedInput[i], 16) shl 4) +
//                    Character.digit(cleanedInput[i + 1], 16)
//                ).toByte()
//        }
//        return data
//    }
}
