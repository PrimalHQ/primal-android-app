package net.primal.android.nostr.utils

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import net.primal.android.crypto.bechToBytes
import net.primal.android.crypto.toHex
import org.junit.Test

class Nip19TLVTest {

    @Test
    fun `tests proper parsing of nevent1`() {
        val nevent1 = "nevent1qqsg6gechd3dhzx38n4z8a2lylzgsmmgeamhmtzz72m9ummsnf0xjfspsdmhxue69" +
                "uhkummn9ekx7mpvwaehxw309ahx7um5wghx77r5wghxgetk93mhxue69uhhyetvv9ujumn0wd68ytn" +
                "zvuk8wumn8ghj7mn0wd68ytn9d9h82mny0fmkzmn6d9njuumsv93k2trhwden5te0wfjkccte9ehx7" +
                "um5wghxyctwvsk8wumn8ghj7un9d3shjtnyv9kh2uewd9hs3kqsdn"

        val eventId = "8d2338bb62db88d13cea23f55f27c4886f68cf777dac42f2b65e6f709a5e6926"

        val tlv = Nip19TLV.parse(nevent1.bechToBytes())

        tlv shouldBe instanceOf( Map::class)
        tlv.size shouldBe 2
        tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex() shouldBe eventId
    }
}
