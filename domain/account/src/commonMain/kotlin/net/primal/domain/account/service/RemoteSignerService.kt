package net.primal.domain.account.service

import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler

interface RemoteSignerService {
    /**
     * Starts continuous observation and interaction with the necessary nostr relays.
     * - Should start `NostrRelayManager` and observe relays for requests.
     * - Should process `NostrEvent`s using `NostrCommandParser` and `NostrCommandHandler`.
     * - Should sign and publish handlers result.
     */
    fun start()

    /**
     * Terminates connection to all relays.
     */
    fun stop()
}
