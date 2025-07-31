package net.primal.android.wallet.zaps

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.networking.relays.FALLBACK_RELAYS
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.domain.mapToRelayDO
import net.primal.android.user.repository.RelayRepository
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.events.EventInteractionRepository
import net.primal.domain.nostr.cryptography.utils.getOrNull
import net.primal.domain.nostr.zaps.ZapError
import net.primal.domain.nostr.zaps.ZapResult
import net.primal.domain.nostr.zaps.ZapTarget

class ZapHandler @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val eventInteractionRepository: EventInteractionRepository,
    private val accountsStore: UserAccountsStore,
    private val relayRepository: RelayRepository,
    private val notary: NostrNotary,
) {

    suspend fun zap(
        userId: String,
        walletId: String,
        target: ZapTarget,
        amountInSats: ULong? = null,
        comment: String? = null,
    ) = withContext(dispatcherProvider.io()) {
        val userAccount = accountsStore.findByIdOrNull(userId = userId)

        val defaultZapOptions = userAccount?.appSettings?.zapDefault
        val zapComment = comment ?: defaultZapOptions?.message ?: ""
        val zapAmountInSats = amountInSats
            ?: defaultZapOptions?.amount?.toULong()
            ?: return@withContext ZapResult.Failure(error = ZapError.InvalidZap(message = "Missing zap amount."))

        val userRelays = relayRepository.findRelays(userId, RelayKind.UserRelay)
            .map { it.mapToRelayDO() }
            .ifEmpty { FALLBACK_RELAYS }

        val userZapRequestEvent = notary.signZapRequestNostrEvent(
            userId = userId,
            comment = zapComment,
            target = target,
            relays = userRelays,
        ).getOrNull() ?: return@withContext ZapResult.Failure(error = ZapError.FailedToSignEvent)

        eventInteractionRepository.zapEvent(
            userId = userId,
            walletId = walletId,
            amountInSats = zapAmountInSats,
            comment = zapComment,
            target = target,
            zapRequestEvent = userZapRequestEvent,
        )
    }
}
