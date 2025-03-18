package net.primal.android.wallet.domain.serializer

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.wallet.domain.WalletKycLevel
import org.junit.Test

class WalletKycLevelSerializerTest {

    @Test
    fun kycLevelNone_encodesToInt0() {
        Json.encodeToString(WalletKycLevel.None) shouldBe "0"
    }

    @Test
    fun kycLevelEmail_encodesToInt2() {
        Json.encodeToString(WalletKycLevel.Email) shouldBe "2"
    }

    @Test
    fun int0_decodesToNoneKycLevel() {
        Json.decodeFromString<WalletKycLevel>("0") shouldBe WalletKycLevel.None
    }

    @Test
    fun int2_decodesToEmailKycLevel() {
        Json.decodeFromString<WalletKycLevel>("2") shouldBe WalletKycLevel.Email
    }

    @Test
    fun stringNONE_decodesToNoneKycLevel() {
        Json.decodeFromJsonElement<WalletKycLevel>(JsonPrimitive("NONE")) shouldBe WalletKycLevel.None
    }

    @Test
    fun stringEMAIL_decodesToEmailKycLevel() {
        Json.decodeFromJsonElement<WalletKycLevel>(JsonPrimitive("EMAIL")) shouldBe WalletKycLevel.Email
    }
}
