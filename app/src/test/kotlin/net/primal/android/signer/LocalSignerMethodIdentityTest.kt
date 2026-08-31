package net.primal.android.signer

import io.kotest.matchers.shouldBe
import net.primal.data.account.signer.local.model.LocalSignerMethod
import net.primal.domain.nostr.NostrUnsignedEvent
import org.junit.Test

class LocalSignerMethodIdentityTest {

    private val pubkeyA = "a".repeat(64)
    private val pubkeyB = "b".repeat(64)

    private fun signEvent(userPubKey: String, eventPubKey: String) =
        LocalSignerMethod.SignEvent(
            eventId = "event-id",
            packageName = "com.example.app",
            requestedAt = 0L,
            userPubKey = userPubKey,
            unsignedEvent = NostrUnsignedEvent(
                pubKey = eventPubKey,
                kind = 1,
                content = "gm",
            ),
        )

    @Test
    fun `sign event with matching identity is consistent`() {
        signEvent(userPubKey = pubkeyA, eventPubKey = pubkeyA)
            .hasConsistentSigningIdentity() shouldBe true
    }

    @Test
    fun `sign event whose author differs from the requested identity is rejected`() {
        signEvent(userPubKey = pubkeyA, eventPubKey = pubkeyB)
            .hasConsistentSigningIdentity() shouldBe false
    }

    @Test
    fun `sign event identity comparison ignores case`() {
        signEvent(userPubKey = pubkeyA.uppercase(), eventPubKey = pubkeyA)
            .hasConsistentSigningIdentity() shouldBe true
    }

    @Test
    fun `non sign methods are always consistent`() {
        LocalSignerMethod.Nip44Decrypt(
            eventId = "event-id",
            packageName = "com.example.app",
            requestedAt = 0L,
            userPubKey = pubkeyA,
            otherPubKey = pubkeyB,
            ciphertext = "ciphertext",
        ).hasConsistentSigningIdentity() shouldBe true
    }
}
