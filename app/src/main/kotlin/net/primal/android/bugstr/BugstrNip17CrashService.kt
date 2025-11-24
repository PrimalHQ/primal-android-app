package net.primal.android.bugstr

import javax.inject.Inject
import javax.inject.Singleton
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.domain.nostr.cryptography.utils.assureValidPubKeyHex
import net.primal.domain.nostr.cryptography.utils.bech32ToHexOrThrow
import com.bugstr.nostr.crypto.Nip17CrashSender
import com.bugstr.nostr.crypto.Nip17Recipient
import com.bugstr.nostr.crypto.Nip17SendRequest

/**
 * Helper to send Bugstr crash reports as NIP-17 gift-wrapped DMs from the active Primal account.
 */
@Singleton
class BugstrNip17CrashService @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val credentialsStore: CredentialsStore,
    private val sender: Nip17CrashSender,
) {
    suspend fun sendCrashReport(
        developerPubKey: String,
        crashReport: String,
        expirationSeconds: Long? = null,
        subject: String? = null,
    ): Result<Unit> {
        val npub = activeAccountStore.activeUserId()
        if (npub.isBlank()) return Result.failure(IllegalStateException("No active user"))

        val credential = runCatching { credentialsStore.findOrThrow(npub = npub) }.getOrElse { return Result.failure(it) }
        val nsec = credential.nsec ?: return Result.failure(IllegalStateException("Active user missing nsec"))

        val senderPubHex = npub.bech32ToHexOrThrow()
        val senderPrivHex = nsec.bech32ToHexOrThrow()
        val recipientHex = developerPubKey.assureValidPubKeyHex()

        val request =
            Nip17SendRequest(
                senderPubKey = senderPubHex,
                senderPrivateKeyHex = senderPrivHex,
                recipients = listOf(Nip17Recipient(pubKeyHex = recipientHex)),
                plaintext = crashReport,
                expirationSeconds = expirationSeconds,
                subject = subject,
            )

        return sender.send(request)
    }
}
