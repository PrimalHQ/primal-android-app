package net.primal.android.nostr.notary

import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.crypto.toHex
import org.junit.Test

class NostrUnsignedEventExtTest {

    @Test
    fun `calculateEventId returns correct id`() {
        val expectedId = "ff1b43e518c16c13ab78ec54b11fb462877822f4596d814efa0e1ab6714c402f"
        val unsignedEvent = NostrUnsignedEvent(
            content = "{\"description\":\"Sync app settings\"}",
            kind = 30078,
            tags = listOf(buildJsonArray {
                add("d")
                add("Primal-Android App")
            }),
            createdAt = 1687881599,
            pubKey = "9b46c3f4a8dcdafdfff12a97c59758f38ff55002370fcfa7d14c8c857e9b5812",
        )

        val actualId = unsignedEvent.calculateEventId().toHex()
        actualId shouldBe expectedId
    }

    @Test
    fun `sign returns correct signature`() {
        val expectedSig = "14e5c298d639ab4285452707d8e6b212e4554b8381bb0a312fa87d6bf5" +
                "ae954bfadf13a9a69b9b5aac3dd37ea6d6020e07ae860e686fce804a408f49ce3c9765"
        val unsignedEvent = NostrUnsignedEvent(
            content = "{\"description\":\"Sync app settings\"}",
            kind = 30078,
            tags = listOf(buildJsonArray {
                add("d")
                add("Primal-Android App")
            }),
            createdAt = 1687881599,
            pubKey = "9b46c3f4a8dcdafdfff12a97c59758f38ff55002370fcfa7d14c8c857e9b5812",
        )

        val signedEvent = unsignedEvent.signOrThrow(
            "nsec18c2dg4s9j7ndlujesf4fq5m3ty6u92jpqffuckf75xyyxqsqy4pstyzq4l"
        )
        signedEvent.sig shouldBe expectedSig
    }

}
