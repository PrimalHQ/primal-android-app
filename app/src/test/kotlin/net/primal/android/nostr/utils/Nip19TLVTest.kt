package net.primal.android.nostr.utils

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import net.primal.android.crypto.toHex
import net.primal.android.crypto.toNpub
import net.primal.android.nostr.utils.Nip19TLV.toNaddrString
import org.junit.Test

class Nip19TLVTest {

    @Test
    fun parse_returnsProperValuesForNevent1() {
        val nevent1 = "nevent1qqsg6gechd3dhzx38n4z8a2lylzgsmmgeamhmtzz72m9ummsnf0xjfspsdmhxue69" +
            "uhkummn9ekx7mpvwaehxw309ahx7um5wghx77r5wghxgetk93mhxue69uhhyetvv9ujumn0wd68ytn" +
            "zvuk8wumn8ghj7mn0wd68ytn9d9h82mny0fmkzmn6d9njuumsv93k2trhwden5te0wfjkccte9ehx7" +
            "um5wghxyctwvsk8wumn8ghj7un9d3shjtnyv9kh2uewd9hs3kqsdn"

        val expectedEventId = "8d2338bb62db88d13cea23f55f27c4886f68cf777dac42f2b65e6f709a5e6926"
        val expectedRelays = "wss://nos.lol,wss://nostr.oxtr.dev,wss://relay.nostr.bg," +
            "wss://nostr.einundzwanzig.space,wss://relay.nostr.band,wss://relay.damus.io"

        val tlv = Nip19TLV.parse(nevent1)

        println(tlv[0]?.first()?.toString())

        tlv shouldBe instanceOf(Map::class)
        tlv.size shouldBe 2
        val actualEventId = tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
        actualEventId shouldBe expectedEventId

        val actualRelay = tlv[Nip19TLV.Type.RELAY.id]?.first()?.let {
            String(bytes = it, charset = Charsets.US_ASCII)
        }
        actualRelay shouldBe expectedRelays
    }

    @Test
    fun parse_returnsProperValuesForNaddr1() {
        val naddr = "naddr1qqw9x6rfwpcxjmn894fks6ts09shyepdg3ty6tthv4unxmf5qy28wumn8ghj7un9d3shjtnyv" +
            "9kh2uewd9hsyg86np9a0kajstc8u9h846rmy6320wdepdeydfz8w8cv7kh9sqv02gpsgqqqw4rsgwawdk"

        val expectedIdentifier = "Shipping-Shipyard-DVM-wey3m4"
        val expectedRelays = "wss://relay.damus.io"
        val expectedProfileId = "npub1l2vyh47mk2p0qlsku7hg0vn29faehy9hy34ygaclpn66ukqp3afqutajft"
        val expectedKind = 30023

        val tlv = Nip19TLV.parse(naddr)
        val actualIdentifier = tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.let {
            String(bytes = it, charset = Charsets.US_ASCII)
        }
        actualIdentifier shouldBe expectedIdentifier

        val actualRelays = tlv[Nip19TLV.Type.RELAY.id]?.first()?.let {
            String(bytes = it, charset = Charsets.US_ASCII)
        }
        actualRelays shouldBe expectedRelays

        val actualProfileId = tlv[Nip19TLV.Type.AUTHOR.id]?.first()?.toNpub()
        actualProfileId shouldBe expectedProfileId

        val actualKind = tlv[Nip19TLV.Type.KIND.id]?.first()?.let {
            Nip19TLV.toInt32(it)
        }
        actualKind shouldBe expectedKind
    }

    @Test
    fun parseAsNaddr_returnsProperValuesForNaddr1() {
        val naddr = "naddr1qqw9x6rfwpcxjmn894fks6ts09shyepdg3ty6tthv4unxmf5qy28wumn8ghj7un9d3shjtnyv" +
            "9kh2uewd9hsyg86np9a0kajstc8u9h846rmy6320wdepdeydfz8w8cv7kh9sqv02gpsgqqqw4rsgwawdk"

        val expectedIdentifier = "Shipping-Shipyard-DVM-wey3m4"
        val expectedRelays = listOf("wss://relay.damus.io")
        val expectedProfileId = "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52"
        val expectedKind = 30023

        val result = Nip19TLV.parseAsNaddr(naddr)
        result.shouldNotBeNull()

        result.identifier shouldBe expectedIdentifier
        result.relays shouldBe expectedRelays
        result.userId shouldBe expectedProfileId
        result.kind shouldBe expectedKind
    }

    @Test
    fun toNaddrString_createsProperNaddr_forGivenNaddrStructureWithoutRelay() {
        val expectedNaddr = "naddr1qqw9x6rfwpcxjmn894fks6ts09shyepdg3ty6tthv4unxmf5qgs04xzt" +
            "6ldm9qhs0ctw0t58kf4z57umjzmjg6jywu0seadwtqqc75srqsqqqa28pl22da"

        val naddr = Naddr(
            identifier = "Shipping-Shipyard-DVM-wey3m4",
            relays = emptyList(),
            userId = "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52",
            kind = 30023,
        )

        naddr.toNaddrString() shouldBe expectedNaddr
    }

    @Test
    fun toNaddrString_createsProperNaddr_forGivenNaddrStructureWithSingleRelay() {
        val expectedNaddr = "naddr1qqw9x6rfwpcxjmn894fks6ts09shyepdg3ty6tthv4unxmf5qy28wumn8ghj7un9d3shjtnyv" +
            "9kh2uewd9hsyg86np9a0kajstc8u9h846rmy6320wdepdeydfz8w8cv7kh9sqv02gpsgqqqw4rsgwawdk"

        val naddr = Naddr(
            identifier = "Shipping-Shipyard-DVM-wey3m4",
            relays = listOf("wss://relay.damus.io"),
            userId = "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52",
            kind = 30023,
        )

        naddr.toNaddrString() shouldBe expectedNaddr
    }

    @Test
    fun toNaddrString_createsProperNaddr_forGivenNaddrStructureWithMultipleRelays() {
        val expectedNaddr = "naddr1qqw9x6rfwpcxjmn894fks6ts09shyepdg3ty6tthv4unxmf5q" +
            "y4hwumn8ghj7un9d3shjtnyv9kh2uewd9hjcamnwvaz7tmjv4kxz7fwwpexjmtpdshxuet5" +
            "qgs04xzt6ldm9qhs0ctw0t58kf4z57umjzmjg6jywu0seadwtqqc75srqsqqqa28zkfejp"

        val naddr = Naddr(
            identifier = "Shipping-Shipyard-DVM-wey3m4",
            relays = listOf("wss://relay.damus.io", "wss://relay.primal.net"),
            userId = "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52",
            kind = 30023,
        )

        naddr.toNaddrString() shouldBe expectedNaddr
    }
}
