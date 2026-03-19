package net.primal.wallet.data.handler

import io.github.aakira.napier.Napier
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.events.EventRepository
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.toNostrString
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.wallet.TxType
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.TxKind
import net.primal.wallet.data.local.dao.EnrichmentAttemptEntry
import net.primal.wallet.data.local.dao.EnrichmentAttemptVerdict
import net.primal.wallet.data.local.dao.ZapEnrichmentStatus
import net.primal.wallet.data.local.dao.ZapEnrichmentTracker
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.local.isEligibleForZapEnrichment
import net.primal.wallet.data.repository.mappers.remote.toNostrEntity

internal class ZapEnrichmentProcessor(
    private val dispatchers: DispatcherProvider,
    private val walletDatabase: WalletDatabase,
    private val eventRepository: EventRepository,
    private val profileRepository: ProfileRepository,
) {
    private val mutex = Mutex()

    suspend fun processEnrichment() {
        // tryLock returns immediately if already locked — we skip rather than suspend.
        // mutex.withLock would suspend until available, which we don't want.
        if (!mutex.tryLock()) return

        try {
            withContext(dispatchers.io()) {
                discoverCandidates()
                enrichEligible()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Napier.w(throwable = e) { "Zap enrichment processing failed" }
        } finally {
            mutex.unlock()
        }
    }

    private suspend fun discoverCandidates() {
        val candidates = walletDatabase.zapEnrichmentTracker().findUntrackedCandidates()
        if (candidates.isEmpty()) return

        val trackers = candidates.map { candidate ->
            ZapEnrichmentTracker(
                transactionId = candidate.transactionId,
                invoice = candidate.invoice,
                transactionCreatedAt = candidate.transactionCreatedAt,
            )
        }
        walletDatabase.zapEnrichmentTracker().insertAllIgnoreConflicts(trackers)
    }

    private suspend fun enrichEligible() {
        val now = Clock.System.now().epochSeconds

        while (true) {
            val batch = walletDatabase.zapEnrichmentTracker().findEligiblePending(
                maxAttempts = MAX_ATTEMPTS,
                threshold1 = now - RETRY_DELAYS_SECONDS[1],
                threshold2 = now - RETRY_DELAYS_SECONDS[2],
                threshold3 = now - RETRY_DELAYS_SECONDS[3],
                threshold4 = now - RETRY_DELAYS_SECONDS[4],
                limit = BATCH_SIZE,
            )
            if (batch.isEmpty()) break

            processBatch(batch, now)
            delay(BATCH_DELAY)
        }
    }

    private suspend fun processBatch(batch: List<ZapEnrichmentTracker>, now: Long) {
        val uniqueInvoices = batch.map { it.invoice }.distinct()

        val zapRequestsResult = eventRepository.getZapRequests(invoices = uniqueInvoices)
        val zapRequests = zapRequestsResult.getOrNull()
        if (zapRequests == null) {
            for (tracker in batch) {
                appendHistory(
                    transactionId = tracker.transactionId,
                    attempt = tracker.attempts,
                    timestamp = now,
                    verdict = EnrichmentAttemptVerdict.NETWORK_ERROR,
                )
            }
            return
        }

        for (tracker in batch) {
            val zapRequest = zapRequests[tracker.invoice]
            if (zapRequest != null) {
                enrichTransaction(tracker, zapRequest, now)
            } else {
                recordFailedAttempt(tracker, now, EnrichmentAttemptVerdict.NO_ZAP_REQUEST)
            }
        }
    }

    private suspend fun enrichTransaction(
        tracker: ZapEnrichmentTracker,
        zapRequest: NostrEvent,
        now: Long,
    ) {
        val zappedEntity = zapRequest.toNostrEntity()
        if (zappedEntity == null) {
            recordFailedAttempt(
                tracker = tracker,
                now = now,
                verdict = EnrichmentAttemptVerdict.MALFORMED_ZAP_REQUEST,
                detail = zapRequest.encodeToJsonString(),
            )
            return
        }

        val transaction = walletDatabase.walletTransactions()
            .findTransactionById(tracker.transactionId) ?: return

        val otherUserId = when (transaction.type) {
            TxType.DEPOSIT -> zapRequest.pubKey
            TxType.WITHDRAW -> zapRequest.tags.findFirstProfileId()
        }

        walletDatabase.withTransaction {
            walletDatabase.walletTransactions().upsertAll(
                data = listOf(
                    transaction.copy(
                        txKind = TxKind.ZAP,
                        zappedEntity = zappedEntity.toNostrString().asEncryptable(),
                        otherUserId = otherUserId?.asEncryptable(),
                        zappedByUserId = zapRequest.pubKey.asEncryptable(),
                        note = zapRequest.content.takeIf { it.isNotBlank() }?.asEncryptable()
                            ?: transaction.note,
                    ),
                ),
            )

            walletDatabase.zapEnrichmentTracker().markEnriched(tracker.transactionId)
        }

        // Best-effort profile fetch — failure doesn't affect enrichment status
        if (otherUserId != null) {
            try {
                profileRepository.fetchProfiles(profileIds = listOf(otherUserId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Napier.w(throwable = e) { "Failed to fetch profile for $otherUserId during enrichment" }
            }
        }
    }

    suspend fun enrichTransaction(transactionId: String): Boolean =
        withContext(dispatchers.io()) {
            val transaction = walletDatabase.walletTransactions()
                .findTransactionById(transactionId) ?: return@withContext false

            if (!transaction.isEligibleForZapEnrichment()) return@withContext false

            val existingTracker = walletDatabase.zapEnrichmentTracker()
                .findByTransactionId(transactionId)

            if (existingTracker != null) {
                if (existingTracker.status == ZapEnrichmentStatus.ENRICHED ||
                    existingTracker.status == ZapEnrichmentStatus.NOT_A_ZAP
                ) {
                    return@withContext false
                }
            } else {
                walletDatabase.zapEnrichmentTracker().insertAllIgnoreConflicts(
                    listOf(
                        ZapEnrichmentTracker(
                            transactionId = transactionId,
                            invoice = transaction.invoice!!,
                            transactionCreatedAt = transaction.createdAt,
                        ),
                    ),
                )
            }

            val tracker = walletDatabase.zapEnrichmentTracker()
                .findByTransactionId(transactionId) ?: return@withContext false

            processTracker(tracker)
        }

    private suspend fun processTracker(tracker: ZapEnrichmentTracker): Boolean {
        val now = Clock.System.now().epochSeconds

        val zapRequestsResult = eventRepository.getZapRequests(invoices = listOf(tracker.invoice))
        val zapRequests = zapRequestsResult.getOrNull()
        if (zapRequests == null) {
            appendHistory(
                transactionId = tracker.transactionId,
                attempt = tracker.attempts,
                timestamp = now,
                verdict = EnrichmentAttemptVerdict.NETWORK_ERROR,
            )
            return false
        }

        val zapRequest = zapRequests[tracker.invoice]
        if (zapRequest != null) {
            enrichTransaction(tracker, zapRequest, now)
            return true
        } else {
            recordFailedAttempt(tracker, now, EnrichmentAttemptVerdict.NO_ZAP_REQUEST)
            return false
        }
    }

    private suspend fun recordFailedAttempt(
        tracker: ZapEnrichmentTracker,
        now: Long,
        verdict: EnrichmentAttemptVerdict,
        detail: String? = null,
    ) {
        appendHistory(
            transactionId = tracker.transactionId,
            attempt = tracker.attempts + 1,
            timestamp = now,
            verdict = verdict,
            detail = detail,
        )
        walletDatabase.zapEnrichmentTracker().incrementAttempt(
            transactionId = tracker.transactionId,
            attemptAt = now,
            maxAttempts = MAX_ATTEMPTS,
        )
    }

    private suspend fun appendHistory(
        transactionId: String,
        attempt: Int,
        timestamp: Long,
        verdict: EnrichmentAttemptVerdict,
        detail: String? = null,
    ) {
        val entry = EnrichmentAttemptEntry(
            attempt = attempt,
            timestamp = timestamp,
            verdict = verdict,
            detail = detail,
        )
        walletDatabase.zapEnrichmentTracker().appendAttemptHistory(
            transactionId = transactionId,
            entryJson = entry.encodeToJsonString(),
        )
    }

    companion object {
        internal const val MAX_ATTEMPTS = 5
        internal const val BATCH_SIZE = 25
        internal val BATCH_DELAY = 500.milliseconds

        internal val RETRY_DELAYS_SECONDS = listOf(
            30.seconds,
            1.minutes,
            10.minutes,
            6.hours,
            1.days,
        ).map { it.inWholeSeconds }
    }
}
