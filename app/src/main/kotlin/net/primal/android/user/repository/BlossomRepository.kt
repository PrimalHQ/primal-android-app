package net.primal.android.user.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.remote.api.users.UsersApi
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asServerTag
import net.primal.domain.publisher.PrimalPublisher
import timber.log.Timber

class BlossomRepository @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val userAccountsStore: UserAccountsStore,
    private val primalPublisher: PrimalPublisher,
    private val usersApi: UsersApi,
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
            try {
                usersApi.getRecommendedBlossomServers()
            } catch (error: WssException) {
                Timber.w(error)
                DEFAULT_BLOSSOM_LIST
            }
        }

    fun getBlossomServers(userId: String): List<String> {
        return userAccountsStore.findByIdOrNull(userId)?.blossomServers ?: DEFAULT_BLOSSOM_LIST
    }
}
