package net.primal.android.user.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asServerTag
import net.primal.domain.publisher.PrimalPublisher

class BlossomRepository @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val userAccountsStore: UserAccountsStore,
    private val primalPublisher: PrimalPublisher,
    private val userRepository: UserRepository,
) {

    private companion object {
        private val DEFAULT_BLOSSOM_LIST = listOf("https://blossom.primal.net")
    }

    suspend fun ensureBlossomServerList(userId: String) {
        val userAccount = userAccountsStore.findByIdOrNull(userId)
        if (userAccount?.blossomServers?.isEmpty() == true) {
            bootstrapBlossomServerList(userId = userId)
        }
    }

    suspend fun bootstrapBlossomServerList(userId: String) {
        publishBlossomServerList(
            userId = userId,
            servers = DEFAULT_BLOSSOM_LIST,
        )
    }

    suspend fun publishBlossomServerList(userId: String, servers: List<String>) {
        withContext(dispatcherProvider.io()) {
            primalPublisher.signPublishImportNostrEvent(
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = NostrEventKind.BlossomServerList.value,
                    tags = servers.map { it.asServerTag() },
                    content = "",
                ),
            )
            userRepository.updateBlossomServers(userId = userId, blossomServers = servers)
        }
    }

    suspend fun fetchSuggestedBlossomList(): List<String> {
        return listOf("https://blossom.primal.net", "https://blossom.nostr.com")
    }

    fun getBlossomServers(userId: String): List<String> {
        return userAccountsStore.findByIdOrNull(userId)?.blossomServers ?: DEFAULT_BLOSSOM_LIST
    }
}
