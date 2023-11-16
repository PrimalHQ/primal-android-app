package net.primal.android.networking.relays

import javax.inject.Inject
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.accounts.BOOTSTRAP_RELAYS
import net.primal.android.user.accounts.UserAccountsStore

class RelaysBootstrapper @Inject constructor(
    private val relaysManager: RelaysManager,
    private val accountsStore: UserAccountsStore,
    private val nostrNotary: NostrNotary,
) {

    suspend fun bootstrap(userId: String) {
        relaysManager.bootstrap()
        relaysManager.publishEvent(
            nostrEvent = nostrNotary.signContactsNostrEvent(
                userId = userId,
                contacts = emptySet(),
                relays = BOOTSTRAP_RELAYS,
            ),
        )
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(relays = BOOTSTRAP_RELAYS)
        }
    }
}
