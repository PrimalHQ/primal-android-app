package net.primal.android.user.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.remote.api.users.UsersApi
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asServerTag
import net.primal.domain.publisher.PrimalPublisher

class BlossomRepository @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val userAccountsStore: UserAccountsStore,
    private val primalPublisher: PrimalPublisher,
    private val usersApi: UsersApi,
) {

    private companion object {
        private val DEFAULT_BLOSSOM_LIST = listOf("https://blossom.primal.net")
    }

    suspend fun ensureBlossomServerList(userId: String): List<String> {
        val userAccount = userAccountsStore.findByIdOrNull(userId)
        val currentList = userAccount?.blossomServers.orEmpty()

        return if (currentList.isEmpty()) {
            bootstrapBlossomServerList(userId)
        } else {
            currentList
        }
    }

    suspend fun bootstrapBlossomServerList(userId: String): List<String> {
        val defaultList = DEFAULT_BLOSSOM_LIST
        publishBlossomServerList(
            userId = userId,
            servers = defaultList,
        )
        return defaultList
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
            persistBlossomServersLocally(userId = userId, blossomServers = servers)
        }
    }

    private suspend fun persistBlossomServersLocally(userId: String, blossomServers: List<String>) {
        userAccountsStore.getAndUpdateAccount(userId) {
            copy(blossomServers = blossomServers)
        }
    }

    suspend fun fetchSuggestedBlossomList(): List<String> =
        withContext(dispatcherProvider.io()) {
            usersApi.getRecommendedBlossomServers()
        }
}
