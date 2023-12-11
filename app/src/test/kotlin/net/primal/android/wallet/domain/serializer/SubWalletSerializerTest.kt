package net.primal.android.wallet.domain.serializer

import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.wallet.domain.SubWallet
import org.junit.Test

class SubWalletSerializerTest {

    @Test
    fun subWalletOpen_encodesToInt1() {
        Json.encodeToString(SubWallet.Open) shouldBe "1"
    }

    @Test
    fun int1_decodesToSubWalletOpen() {
        Json.decodeFromString<SubWallet>("1") shouldBe SubWallet.Open
    }

    @Test
    fun stringZAPPING_decodesToSubWalletOpen() {
        Json.decodeFromJsonElement<SubWallet>(JsonPrimitive("ZAPPING")) shouldBe SubWallet.Open
    }
}
