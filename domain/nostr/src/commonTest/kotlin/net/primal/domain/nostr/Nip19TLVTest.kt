package net.primal.domain.nostr

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import kotlin.test.Test
import net.primal.domain.nostr.Nip19TLV.readAsString
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.Nip19TLV.toNeventString
import net.primal.domain.nostr.Nip19TLV.toNprofileString
import net.primal.domain.nostr.cryptography.utils.toHex
import net.primal.domain.nostr.cryptography.utils.toNpub

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

        tlv shouldBe instanceOf(Map::class)
        tlv.size shouldBe 2
        val actualEventId = tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
        actualEventId shouldBe expectedEventId

        val actualRelay = tlv[Nip19TLV.Type.RELAY.id]?.first()?.readAsString()
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
        val actualIdentifier = tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.readAsString()
        actualIdentifier shouldBe expectedIdentifier

        val actualRelays = tlv[Nip19TLV.Type.RELAY.id]?.first()?.readAsString()
        actualRelays shouldBe expectedRelays

        val actualProfileId = tlv[Nip19TLV.Type.AUTHOR.id]?.first()?.toNpub()
        actualProfileId shouldBe expectedProfileId

        val actualKind = tlv[Nip19TLV.Type.KIND.id]?.first()?.let {
            Nip19TLV.toInt32(it)
        }
        actualKind shouldBe expectedKind
    }

    @Test
    fun parseUriAsNaddrOrNull_returnsProperValuesForNaddr1Uri() {
        val naddr = "nostr:naddr1qqw9x6rfwpcxjmn894fks6ts09shyepdg3ty6tthv4unxmf5qy28wumn8ghj7un9d3shjtnyv" +
            "9kh2uewd9hsyg86np9a0kajstc8u9h846rmy6320wdepdeydfz8w8cv7kh9sqv02gpsgqqqw4rsgwawdk"

        val expectedIdentifier = "Shipping-Shipyard-DVM-wey3m4"
        val expectedRelays = listOf("wss://relay.damus.io")
        val expectedProfileId = "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52"
        val expectedKind = 30023

        val result = Nip19TLV.parseUriAsNaddrOrNull(naddr)
        result.shouldNotBeNull()

        result.identifier shouldBe expectedIdentifier
        result.relays shouldBe expectedRelays
        result.userId shouldBe expectedProfileId
        result.kind shouldBe expectedKind
    }

    @Test
    fun parseUriAsNaddrOrNull_returnsProperValuesForNaddr1WithoutNostrScheme() {
        val naddr = "naddr1qqw9x6rfwpcxjmn894fks6ts09shyepdg3ty6tthv4unxmf5qy28wumn8ghj7un9d3shjtnyv" +
            "9kh2uewd9hsyg86np9a0kajstc8u9h846rmy6320wdepdeydfz8w8cv7kh9sqv02gpsgqqqw4rsgwawdk"

        val expectedIdentifier = "Shipping-Shipyard-DVM-wey3m4"
        val expectedRelays = listOf("wss://relay.damus.io")
        val expectedProfileId = "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52"
        val expectedKind = 30023

        val result = Nip19TLV.parseUriAsNaddrOrNull(naddr)
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
            "y28wumn8ghj7un9d3shjtnyv9kh2uewd9hsz9nhwden5te0wfjkccte9ec8y6tdv9kzumn9" +
            "wspzp75cf0tahv5z7plpdeaws7ex52nmnwgtwfr2g3m37r844evqrr6jqvzqqqr4gux34syq"

        val naddr = Naddr(
            identifier = "Shipping-Shipyard-DVM-wey3m4",
            relays = listOf("wss://relay.damus.io", "wss://relay.primal.net"),
            userId = "fa984bd7dbb282f07e16e7ae87b26a2a7b9b90b7246a44771f0cf5ae58018f52",
            kind = 30023,
        )

        naddr.toNaddrString() shouldBe expectedNaddr
    }

    @Test
    fun parseUriAsNeventOrNull_returnsProperValuesForNoAuthorAndNeventNoUris() {
        val nevent = "nostr:nevent1qvzqqqpxfgqzq4zfn02zu4cdg2xwrt8atpp2v8sfu4c2xwwxsx2llnx0d8xekj8vvs3qvg"

        val expectedEventId = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec"
        val expectedRelays = emptyList<String>()
        val expectedUserId = null
        val expectedKind = 9802

        val result = Nip19TLV.parseUriAsNeventOrNull(nevent)
        result.shouldNotBeNull()

        result.eventId shouldBe expectedEventId
        result.relays shouldBe expectedRelays
        result.userId shouldBe expectedUserId
        result.kind shouldBe expectedKind
    }

    @Test
    fun parseUriAsNeventOrNull_returnsProperValuesForNeventNoUris() {
        val nevent = "nostr:nevent1qqs9gjvm6sh9wr2z3ns6el2cg2npuz09wz3nn35pjhluenmfekd53mqzyrtp7w" +
            "79k045gq80mtnpdxjuzl9t7vjxk52rv80f888y5xsd5mh55qcyqqqzvjsk2whrp"

        val expectedEventId = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec"
        val expectedRelays = emptyList<String>()
        val expectedUserId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a"
        val expectedKind = 9802

        val result = Nip19TLV.parseUriAsNeventOrNull(nevent)
        result.shouldNotBeNull()

        result.eventId shouldBe expectedEventId
        result.relays shouldBe expectedRelays
        result.userId shouldBe expectedUserId
        result.kind shouldBe expectedKind
    }

    @Test
    fun parseUriAsNeventOrNull_returnsProperValuesForNeventSingleUri() {
        val nevent = "nostr:nevent1qvzqqqpxfgpzp4sl80zm866yqrha4esknfwp0j4lxfrt2" +
            "9pkrh5nnnj2rgx6dm62qyv8wumn8ghj7urjv4kkjatd9ec8y6tdv9kzumn9wsqzq4zf" +
            "n02zu4cdg2xwrt8atpp2v8sfu4c2xwwxsx2llnx0d8xekj8v6xeest"

        val expectedEventId = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec"
        val expectedRelays = listOf("wss://premium.primal.net")
        val expectedUserId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a"
        val expectedKind = 9802

        val result = Nip19TLV.parseUriAsNeventOrNull(nevent)
        result.shouldNotBeNull()

        result.eventId shouldBe expectedEventId
        result.relays shouldBe expectedRelays
        result.userId shouldBe expectedUserId
        result.kind shouldBe expectedKind
    }

    @Test
    fun parseUriAsNeventOrNull_returnsProperValuesForNeventMultipleUris() {
        val nevent = "nostr:nevent1qvzqqqpxfgpzp4sl80zm866yqrha4esknfwp0j4l" +
            "xfrt29pkrh5nnnj2rgx6dm62qy28wumn8ghj7un9d3shjtnyv9kh2uewd9hszx" +
            "rhwden5te0wpex2mtfw4kjuurjd9kkzmpwdejhgqpq23yeh4pw2ux59r8p4n74ss4xrcy72u9r88rgr90len8knnvmfrkqe7q9g2"

        val expectedEventId = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec"
        val expectedRelays = listOf("wss://relay.damus.io", "wss://premium.primal.net")
        val expectedUserId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a"
        val expectedKind = 9802

        val result = Nip19TLV.parseUriAsNeventOrNull(nevent)
        result.shouldNotBeNull()

        result.eventId shouldBe expectedEventId
        result.relays shouldBe expectedRelays
        result.userId shouldBe expectedUserId
        result.kind shouldBe expectedKind
    }

    @Test
    fun toNeventString_createsProperNevent_forGivenNeventStructureWithoutRelaysAndNoAuthor() {
        val expectedNevent = "nevent1qqs9gjvm6sh9wr2z3ns6el2cg2npuz09wz3nn35pjhluenmfekd53mqrqsqqqfj27mfm7c"

        val nevent = Nevent(
            eventId = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec",
            relays = emptyList(),
            userId = null,
            kind = 9802,
        )

        nevent.toNeventString() shouldBe expectedNevent
    }

    @Test
    fun toNeventString_createsProperNevent_forGivenNeventStructureWithoutRelays() {
        val expectedNevent = "nevent1qqs9gjvm6sh9wr2z3ns6el2cg2npuz09wz3nn35pjhluenmfekd53mqzyrtp7w79k045g" +
            "q80mtnpdxjuzl9t7vjxk52rv80f888y5xsd5mh55qcyqqqzvjsk2whrp"

        val nevent = Nevent(
            eventId = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec",
            relays = emptyList(),
            userId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a",
            kind = 9802,
        )

        nevent.toNeventString() shouldBe expectedNevent
    }

    @Test
    fun toNeventString_createsProperNevent_forGivenNeventStructureWithSingleRelay() {
        val expectedNevent = "nevent1qqs9gjvm6sh9wr2z3ns6el2cg2npuz09wz3nn35pjhluenmfekd53mqpr9mhxue69uhhqun" +
            "9d45h2mfwwpexjmtpdshxuet59upzp4sl80zm866yqrha4esknfwp0j4lxfrt29pkrh5nnnj2rgx6dm62qvzqqqpxfg8l385v"

        val nevent = Nevent(
            eventId = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec",
            relays = listOf("wss://premium.primal.net/"),
            userId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a",
            kind = 9802,
        )

        nevent.toNeventString() shouldBe expectedNevent
    }

    @Test
    fun toNeventString_createsProperNevent_forGivenNeventStructureWithMultipleRelays() {
        val expectedNevent = "nevent1qqs9gjvm6sh9wr2z3ns6el2cg2npuz09wz3nn35pjhluenm" +
            "fekd53mqpz3mhxue69uhhyetvv9ujuerpd46hxtnfduq3samnwvaz7tmswfjk66t4d5h8qu" +
            "nfd4skctnwv46qygxkruautvltgsqwlkhxz6d9c972hueyddg5xcw7jwwwfgdqmfh0fgpsgqqqye9qaww523"

        val nevent = Nevent(
            eventId = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec",
            relays = listOf("wss://relay.damus.io", "wss://premium.primal.net"),
            userId = "d61f3bc5b3eb4400efdae6169a5c17cabf3246b514361de939ce4a1a0da6ef4a",
            kind = 9802,
        )

        nevent.toNeventString() shouldBe expectedNevent
    }

    @Test
    fun parseUriAsNprofileOrNull_returnsProperValuesForNeventNoUris() {
        val nprofile = "nostr:nprofile1qqs9gjvm6sh9wr2z3ns6el2cg2npuz09wz3nn35pjhluenmfekd53mqxg4j84"

        val expectedPubkey = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec"
        val expectedRelays = emptyList<String>()

        val result = Nip19TLV.parseUriAsNprofileOrNull(nprofile)

        result.shouldNotBeNull()
        result.pubkey shouldBe expectedPubkey
        result.relays shouldBe expectedRelays
    }

    @Test
    fun parseUriAsNprofileOrNull_returnsProperValuesForNeventSingleUri() {
        val nprofile = "nostr:nprofile1qyv8wumn8ghj7urjv4kkjatd9ec8y6tdv9" +
            "kzumn9wsqzq4zfn02zu4cdg2xwrt8atpp2v8sfu4c2xwwxsx2llnx0d8xekj8v8ee8fv"

        val expectedPubkey = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec"
        val expectedRelays = listOf("wss://premium.primal.net")

        val result = Nip19TLV.parseUriAsNprofileOrNull(nprofile)

        result.shouldNotBeNull()
        result.pubkey shouldBe expectedPubkey
        result.relays shouldBe expectedRelays
    }

    @Test
    fun parseUriAsNprofileOrNull_returnsProperValuesForNeventMultipleUris() {
        val nprofile = "nostr:nprofile1qyv8wumn8ghj7urjv4kkjatd9ec8y6tdv9kzu" +
            "mn9wsq3gamnwvaz7tmjv4kxz7fwv3sk6atn9e5k7qpq23yeh4pw2ux59r8p4n74ss4xrcy72u9r88rgr90len8knnvmfrkq3s8k4w"

        val expectedPubkey = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec"
        val expectedRelays = listOf("wss://premium.primal.net", "wss://relay.damus.io")

        val result = Nip19TLV.parseUriAsNprofileOrNull(nprofile)

        result.shouldNotBeNull()
        result.pubkey shouldBe expectedPubkey
        result.relays shouldBe expectedRelays
    }

    @Test
    fun toNprofileString_createsProperNprofile_forGivenNprofileStructureWithoutRelays() {
        val expectedNprofile = "nprofile1qqs9gjvm6sh9wr2z3ns6el2cg2npuz09wz3nn35pjhluenmfekd53mqxg4j84"

        val nprofile = Nprofile(
            pubkey = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec",
        )

        nprofile.toNprofileString() shouldBe expectedNprofile
    }

    @Test
    fun toNprofileString_createsProperNprofile_forGivenNprofileStructureWithSingleRelay() {
        val expectedNprofile = "nprofile1qqs9gjvm6sh9wr2z3ns6el2cg2npuz09wz3n" +
            "n35pjhluenmfekd53mqprpmhxue69uhhqun9d45h2mfwwpexjmtpdshxuet5n9zrjx"

        val nprofile = Nprofile(
            pubkey = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec",
            relays = listOf("wss://premium.primal.net"),
        )

        nprofile.toNprofileString() shouldBe expectedNprofile
    }

    @Test
    fun toNprofileString_createsProperNprofile_forGivenNprofileStructureWithMultipleRelays() {
        val expectedNprofile = "nprofile1qqs9gjvm6sh9wr2z3ns6el2cg2npuz09wz3nn35p" +
            "jhluenmfekd53mqprpmhxue69uhhqun9d45h2mfwwpexjmtpdshxuet5qy28wumn8ghj7un9d3shjtnyv9kh2uewd9hsqa5sds"

        val nprofile = Nprofile(
            pubkey = "54499bd42e570d428ce1acfd5842a61e09e570a339c68195ffcccf69cd9b48ec",
            relays = listOf("wss://premium.primal.net", "wss://relay.damus.io"),
        )

        nprofile.toNprofileString() shouldBe expectedNprofile
    }
}
