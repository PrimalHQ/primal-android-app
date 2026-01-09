package net.primal.data.repository.broadcast

import kotlinx.coroutines.withContext
import net.primal.core.networking.UserAgentProvider
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.runCatching
import net.primal.data.remote.api.premium.PremiumBroadcastApi
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.domain.premium.BroadcastingStatus
import net.primal.domain.premium.PremiumBroadcastRepository

class PremiumBroadcastRepositoryImpl(
    private val dispatchers: DispatcherProvider,
    private val premiumBroadcastApi: PremiumBroadcastApi,
    private val nostrEventSignatureHandler: NostrEventSignatureHandler,
) : PremiumBroadcastRepository {
    override suspend fun fetchContentStats(userId: String): Result<Map<Int, Long>> =
        withContext(dispatchers.io()) {
            runCatching {
                premiumBroadcastApi.getContentStats(userId, buildAppSpecificDataNostrEvent(userId))
            }
        }

    override suspend fun startBroadcast(userId: String, kinds: List<Int>?) =
        withContext(dispatchers.io()) {
            runCatching {
                premiumBroadcastApi.startContentRebroadcast(userId, kinds, buildAppSpecificDataNostrEvent(userId))
            }
        }

    override suspend fun cancelBroadcast(userId: String) =
        withContext(dispatchers.io()) {
            runCatching {
                premiumBroadcastApi.cancelContentRebroadcast(userId, buildAppSpecificDataNostrEvent(userId))
            }
        }

    override suspend fun fetchBroadcastStatus(userId: String): Result<BroadcastingStatus> =
        withContext(dispatchers.io()) {
            runCatching {
                premiumBroadcastApi.getContentRebroadcastStatus(userId, buildAppSpecificDataNostrEvent(userId))
            }
        }

    private suspend fun buildAppSpecificDataNostrEvent(userId: String) =
        nostrEventSignatureHandler.signNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.ApplicationSpecificData.value,
                tags = listOf("${UserAgentProvider.resolveUserAgent()} App".asIdentifierTag()),
                content = "{}",
            ),
        ).unwrapOrThrow()
}
