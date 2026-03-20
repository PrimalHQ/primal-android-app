package net.primal.wallet.data.handler

import android.content.Context
import androidx.paging.PagingData
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.common.UserProfileSearchItem
import net.primal.domain.events.EventRepository
import net.primal.domain.events.EventZap
import net.primal.domain.events.NostrEventAction
import net.primal.domain.events.NostrEventStats
import net.primal.domain.events.NostrEventUserStats
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.ReportType
import net.primal.domain.profile.ProfileData
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.profile.ProfileStats
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.ENRICHMENT_CUTOFF_EPOCH_SECONDS
import net.primal.wallet.data.local.TxKind
import net.primal.wallet.data.local.dao.EnrichmentAttemptEntry
import net.primal.wallet.data.local.dao.EnrichmentAttemptVerdict
import net.primal.wallet.data.local.dao.WalletTransactionData
import net.primal.wallet.data.local.dao.ZapEnrichmentStatus
import net.primal.wallet.data.local.dao.ZapEnrichmentTracker
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.local.isEligibleForZapEnrichment
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ZapEnrichmentProcessorTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider = object : DispatcherProvider {
        override fun io() = testDispatcher
        override fun main() = testDispatcher
    }

    private lateinit var database: WalletDatabase
    private lateinit var fakeEventRepository: FakeEventRepository
    private lateinit var fakeProfileRepository: FakeProfileRepository
    private lateinit var processor: ZapEnrichmentProcessor

    @Before
    fun setUp() {
        WalletDatabase.setEncryption(enableEncryption = false)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "wallet_zap_enrichment_test_${UUID.randomUUID()}.db"
        database = Room.databaseBuilder<WalletDatabase>(
            context = context,
            name = dbName,
        )
            .setDriver(AndroidSQLiteDriver())
            .allowMainThreadQueries()
            .build()

        fakeEventRepository = FakeEventRepository()
        fakeProfileRepository = FakeProfileRepository()
        processor = ZapEnrichmentProcessor(
            dispatchers = dispatcherProvider,
            walletDatabase = database,
            eventRepository = fakeEventRepository,
            profileRepository = fakeProfileRepository,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    // region Discovery tests

    @Test
    fun discoveryCreatesPendingTrackersForLightningWithInvoice() =
        runTest {
            insertTransaction(txKind = TxKind.LIGHTNING, invoice = "lnbc1invoice")

            processor.processEnrichment()

            val trackers = allPendingTrackers()
            trackers shouldHaveSize 1
            trackers.first().status shouldBe ZapEnrichmentStatus.PENDING
        }

    @Test
    fun discoveryCreatesPendingTrackersForSparkWithInvoice() =
        runTest {
            insertTransaction(txKind = TxKind.SPARK, invoice = "lnbc2invoice")

            processor.processEnrichment()

            val trackers = allPendingTrackers()
            trackers shouldHaveSize 1
            trackers.first().status shouldBe ZapEnrichmentStatus.PENDING
        }

    @Test
    fun discoverySkipsZapOnChainStorePurchaseTransactions() =
        runTest {
            insertTransaction(txKind = TxKind.ZAP, invoice = "lnbc-zap")
            insertTransaction(txKind = TxKind.ON_CHAIN, invoice = "lnbc-onchain")
            insertTransaction(txKind = TxKind.STORE_PURCHASE, invoice = "lnbc-store")

            processor.processEnrichment()

            val trackers = allPendingTrackers()
            trackers.shouldBeEmpty()
        }

    @Test
    fun discoverySkipsTransactionsWithNullInvoice() =
        runTest {
            insertTransaction(txKind = TxKind.LIGHTNING, invoice = null)

            processor.processEnrichment()

            val trackers = allPendingTrackers()
            trackers.shouldBeEmpty()
        }

    @Test
    fun discoverySkipsPrimalWalletTransactions() =
        runTest {
            insertTransaction(txKind = TxKind.LIGHTNING, invoice = "lnbc-primal", walletType = WalletType.PRIMAL)

            processor.processEnrichment()

            val trackers = allPendingTrackers()
            trackers.shouldBeEmpty()
        }

    @Test
    fun discoverySkipsAlreadyTrackedTransactions() =
        runTest {
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = "lnbc-tracked")

            // First pass creates tracker
            processor.processEnrichment()
            val trackersAfterFirst = allPendingTrackers()
            trackersAfterFirst shouldHaveSize 1

            // Second pass should not duplicate
            processor.processEnrichment()
            val trackersAfterSecond = allPendingTrackers()
            trackersAfterSecond shouldHaveSize 1
        }

    @Test
    fun discoverySkipsSparkTransactionsBeforeCutoffDate() =
        runTest {
            insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = "lnbc-old-spark",
                walletType = WalletType.SPARK,
                createdAt = ENRICHMENT_CUTOFF_EPOCH_SECONDS - 1,
            )

            processor.processEnrichment()

            val trackers = allPendingTrackers()
            trackers.shouldBeEmpty()
        }

    @Test
    fun discoveryIncludesSparkTransactionsAtCutoffDate() =
        runTest {
            insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = "lnbc-boundary-spark",
                walletType = WalletType.SPARK,
                createdAt = ENRICHMENT_CUTOFF_EPOCH_SECONDS,
            )

            processor.processEnrichment()

            val trackers = allPendingTrackers()
            trackers shouldHaveSize 1
        }

    @Test
    fun discoveryIncludesSparkTransactionsAfterCutoffDate() =
        runTest {
            insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = "lnbc-new-spark",
                walletType = WalletType.SPARK,
                createdAt = ENRICHMENT_CUTOFF_EPOCH_SECONDS + 1,
            )

            processor.processEnrichment()

            val trackers = allPendingTrackers()
            trackers shouldHaveSize 1
        }

    @Test
    fun discoveryIncludesNwcTransactionsBeforeCutoffDate() =
        runTest {
            insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = "lnbc-old-nwc",
                walletType = WalletType.NWC,
                createdAt = ENRICHMENT_CUTOFF_EPOCH_SECONDS - 1,
            )

            processor.processEnrichment()

            val trackers = allPendingTrackers()
            trackers shouldHaveSize 1
        }

    @Test
    fun discoveryIncludesNwcTransactionsAfterCutoffDate() =
        runTest {
            insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = "lnbc-new-nwc",
                walletType = WalletType.NWC,
                createdAt = ENRICHMENT_CUTOFF_EPOCH_SECONDS + 1,
            )

            processor.processEnrichment()

            val trackers = allPendingTrackers()
            trackers shouldHaveSize 1
        }

    @Test
    fun discoverySkipsPrimalWalletTransactionsAfterCutoffDate() =
        runTest {
            insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = "lnbc-primal-post-cutoff",
                walletType = WalletType.PRIMAL,
                createdAt = ENRICHMENT_CUTOFF_EPOCH_SECONDS + 1,
            )

            processor.processEnrichment()

            val trackers = allPendingTrackers()
            trackers.shouldBeEmpty()
        }

    @Test
    fun emptyDatabaseProducesNoApiCalls() =
        runTest {
            processor.processEnrichment()

            fakeEventRepository.getZapRequestsCallCount shouldBe 0
        }

    // endregion

    // region Enrichment tests

    @Test
    fun successfulEnrichmentUpdatesTransactionToZapKind() =
        runTest {
            val invoice = "lnbc-enrich-1"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            val zapRequest = buildZapRequestEvent()
            fakeEventRepository.zapRequestsToReturn = mapOf(invoice to zapRequest)

            processor.processEnrichment()

            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.txKind shouldBe TxKind.ZAP

            val tracker = allTrackers().first { it.transactionId == txId }
            tracker.status shouldBe ZapEnrichmentStatus.ENRICHED
        }

    @Test
    fun enrichmentOverwritesExistingNoteWithZapContent() =
        runTest {
            val invoice = "lnbc-note-overwrite"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            val zapRequest = buildZapRequestEvent(content = "Zap content!")
            fakeEventRepository.zapRequestsToReturn = mapOf(invoice to zapRequest)

            processor.processEnrichment()

            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.note?.decrypted shouldBe "Zap content!"
        }

    @Test
    fun enrichmentUsesZapPubKeyAsOtherUserIdForDeposit() =
        runTest {
            val invoice = "lnbc-deposit"
            val txId = insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = invoice,
                txType = TxType.DEPOSIT,
            )
            val zapRequest = buildZapRequestEvent(pubKey = "sender-abc")
            fakeEventRepository.zapRequestsToReturn = mapOf(invoice to zapRequest)

            processor.processEnrichment()

            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.otherUserId?.decrypted shouldBe "sender-abc"
        }

    @Test
    fun enrichmentUsesProfileTagAsOtherUserIdForWithdraw() =
        runTest {
            val invoice = "lnbc-withdraw"
            val txId = insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = invoice,
                txType = TxType.WITHDRAW,
            )
            val zapRequest = buildZapRequestEvent(
                pubKey = "sender-xyz",
                profileId = "receiver-target",
            )
            fakeEventRepository.zapRequestsToReturn = mapOf(invoice to zapRequest)

            processor.processEnrichment()

            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.otherUserId?.decrypted shouldBe "receiver-target"
        }

    @Test
    fun failedEnrichmentIncrementsAttemptsAndLeavesTransactionUntouched() =
        runTest {
            val invoice = "lnbc-miss"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            // No zap request returned for this invoice
            fakeEventRepository.zapRequestsToReturn = emptyMap()

            processor.processEnrichment()

            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.txKind shouldBe TxKind.LIGHTNING

            val tracker = allTrackers().first { it.transactionId == txId }
            tracker.attempts shouldBe 1
            tracker.status shouldBe ZapEnrichmentStatus.PENDING
        }

    @Test
    fun afterMaxAttemptsTrackerIsMarkedNotAZap() =
        runTest {
            val invoice = "lnbc-max-attempts"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            fakeEventRepository.zapRequestsToReturn = emptyMap()

            repeat(ZapEnrichmentProcessor.MAX_ATTEMPTS) {
                backdateTracker(txId)
                processor.processEnrichment()
            }

            val tracker = allTrackers().first { it.transactionId == txId }
            tracker.status shouldBe ZapEnrichmentStatus.NOT_A_ZAP
            tracker.attempts shouldBe ZapEnrichmentProcessor.MAX_ATTEMPTS
        }

    @Test
    fun apiFailureDoesNotIncrementAttempts() =
        runTest {
            val invoice = "lnbc-api-fail"
            insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            fakeEventRepository.shouldFail = true

            processor.processEnrichment()

            val tracker = allTrackers().first()
            tracker.attempts shouldBe 0
            tracker.status shouldBe ZapEnrichmentStatus.PENDING
        }

    @Test
    fun profileFetchFailureDoesNotAffectEnrichmentStatus() =
        runTest {
            val invoice = "lnbc-profile-fail"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            val zapRequest = buildZapRequestEvent()
            fakeEventRepository.zapRequestsToReturn = mapOf(invoice to zapRequest)
            fakeProfileRepository.shouldFail = true

            processor.processEnrichment()

            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.txKind shouldBe TxKind.ZAP

            val tracker = allTrackers().first { it.transactionId == txId }
            tracker.status shouldBe ZapEnrichmentStatus.ENRICHED
            fakeProfileRepository.fetchProfilesCallCount shouldBe 1
        }

    @Test
    fun mixedResultsEnrichesHitsAndIncrementsAttemptsForMisses() =
        runTest {
            val invoiceHit = "lnbc-hit"
            val invoiceMiss = "lnbc-miss-mix"
            val txIdHit = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoiceHit)
            val txIdMiss = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoiceMiss)
            val zapRequest = buildZapRequestEvent()
            fakeEventRepository.zapRequestsToReturn = mapOf(invoiceHit to zapRequest)

            processor.processEnrichment()

            val txHit = database.walletTransactions().findTransactionById(txIdHit)!!
            txHit.txKind shouldBe TxKind.ZAP

            val txMiss = database.walletTransactions().findTransactionById(txIdMiss)!!
            txMiss.txKind shouldBe TxKind.LIGHTNING

            val trackerHit = allTrackers().first { it.transactionId == txIdHit }
            trackerHit.status shouldBe ZapEnrichmentStatus.ENRICHED

            val trackerMiss = allTrackers().first { it.transactionId == txIdMiss }
            trackerMiss.status shouldBe ZapEnrichmentStatus.PENDING
            trackerMiss.attempts shouldBe 1
        }

    @Test
    fun multiAccountSameInvoiceBothGetEnriched() =
        runTest {
            val invoice = "lnbc-shared"
            val txId1 = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            val txId2 = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            val zapRequest = buildZapRequestEvent()
            fakeEventRepository.zapRequestsToReturn = mapOf(invoice to zapRequest)

            processor.processEnrichment()

            val tx1 = database.walletTransactions().findTransactionById(txId1)!!
            tx1.txKind shouldBe TxKind.ZAP

            val tx2 = database.walletTransactions().findTransactionById(txId2)!!
            tx2.txKind shouldBe TxKind.ZAP

            // Same invoice should be deduplicated in the API call
            fakeEventRepository.getZapRequestsCallCount shouldBe 1
        }

    @Test
    fun alreadyEnrichedTransactionsAreNeverReprocessed() =
        runTest {
            val invoice = "lnbc-already-enriched"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            val zapRequest = buildZapRequestEvent()
            fakeEventRepository.zapRequestsToReturn = mapOf(invoice to zapRequest)

            // First pass enriches
            processor.processEnrichment()
            fakeEventRepository.getZapRequestsCallCount shouldBe 1

            // Second pass should not call API again
            processor.processEnrichment()
            fakeEventRepository.getZapRequestsCallCount shouldBe 1
        }

    @Test
    fun alreadyNotAZapTransactionsAreNeverReprocessed() =
        runTest {
            val invoice = "lnbc-not-a-zap"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            fakeEventRepository.zapRequestsToReturn = emptyMap()

            // Exhaust all attempts
            repeat(ZapEnrichmentProcessor.MAX_ATTEMPTS) {
                backdateTracker(txId)
                processor.processEnrichment()
            }

            val callCountAfterMax = fakeEventRepository.getZapRequestsCallCount
            callCountAfterMax shouldBe ZapEnrichmentProcessor.MAX_ATTEMPTS

            // One more pass should NOT call API
            processor.processEnrichment()
            fakeEventRepository.getZapRequestsCallCount shouldBe callCountAfterMax
        }

    @Test
    fun retrySpacingSkipsAttemptsWhenInsufficientTimeHasPassed() =
        runTest {
            val invoice = "lnbc-retry-spacing"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            fakeEventRepository.zapRequestsToReturn = emptyMap()

            // First attempt — immediate, should process
            processor.processEnrichment()
            fakeEventRepository.getZapRequestsCallCount shouldBe 1

            // Second attempt without backdating — lastAttemptAt is recent, should NOT process
            processor.processEnrichment()
            fakeEventRepository.getZapRequestsCallCount shouldBe 1

            // Backdate lastAttemptAt to make it eligible again
            backdateTracker(txId)
            processor.processEnrichment()
            fakeEventRepository.getZapRequestsCallCount shouldBe 2
        }

    @Test
    fun largeCandidateSetProcessedInBatches() =
        runTest(testDispatcher) {
            val totalTxs = 60
            val zapRequests = mutableMapOf<String, NostrEvent>()
            repeat(totalTxs) { i ->
                val invoice = "lnbc-batch-$i"
                insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
                zapRequests[invoice] = buildZapRequestEvent()
            }
            fakeEventRepository.zapRequestsToReturn = zapRequests

            processor.processEnrichment()

            val expectedBatches = (totalTxs + ZapEnrichmentProcessor.BATCH_SIZE - 1) / ZapEnrichmentProcessor.BATCH_SIZE
            fakeEventRepository.getZapRequestsCallCount shouldBe expectedBatches

            // All should be enriched
            val enriched = allTrackers().filter { it.status == ZapEnrichmentStatus.ENRICHED }
            enriched shouldHaveSize totalTxs
        }

    // endregion

    // region Single-transaction enrichment tests

    @Test
    fun enrichTransactionWithZapReceiptReturnsTrue() =
        runTest {
            val invoice = "lnbc-single-hit"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            val zapRequest = buildZapRequestEvent()
            fakeEventRepository.zapRequestsToReturn = mapOf(invoice to zapRequest)

            val result = processor.enrichTransaction(txId)

            result shouldBe true
            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.txKind shouldBe TxKind.ZAP
            val tracker = database.zapEnrichmentTracker().findByTransactionId(txId)!!
            tracker.status shouldBe ZapEnrichmentStatus.ENRICHED
        }

    @Test
    fun enrichTransactionWithoutZapReceiptReturnsFalseAndIncrementsAttempts() =
        runTest {
            val invoice = "lnbc-single-miss"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            fakeEventRepository.zapRequestsToReturn = emptyMap()

            val result = processor.enrichTransaction(txId)

            result shouldBe false
            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.txKind shouldBe TxKind.LIGHTNING
            val tracker = database.zapEnrichmentTracker().findByTransactionId(txId)!!
            tracker.attempts shouldBe 1
        }

    @Test
    fun enrichTransactionAlreadyEnrichedReturnsFalseNoApiCall() =
        runTest {
            val invoice = "lnbc-single-already-enriched"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            val zapRequest = buildZapRequestEvent()
            fakeEventRepository.zapRequestsToReturn = mapOf(invoice to zapRequest)

            // First call enriches
            processor.enrichTransaction(txId)
            val callCountAfter = fakeEventRepository.getZapRequestsCallCount

            // Second call should bail early
            val result = processor.enrichTransaction(txId)

            result shouldBe false
            fakeEventRepository.getZapRequestsCallCount shouldBe callCountAfter
        }

    @Test
    fun enrichTransactionNotAZapReturnsFalseNoApiCall() =
        runTest {
            val invoice = "lnbc-single-not-a-zap"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            fakeEventRepository.zapRequestsToReturn = emptyMap()

            // Exhaust all attempts via batch to mark NOT_A_ZAP
            repeat(ZapEnrichmentProcessor.MAX_ATTEMPTS) {
                backdateTracker(txId)
                processor.processEnrichment()
            }
            val callCountAfter = fakeEventRepository.getZapRequestsCallCount

            val result = processor.enrichTransaction(txId)

            result shouldBe false
            fakeEventRepository.getZapRequestsCallCount shouldBe callCountAfter
        }

    @Test
    fun enrichTransactionNonEligibleReturnsFalseNoApiCall() =
        runTest {
            val zapTxId = insertTransaction(txKind = TxKind.ZAP, invoice = "lnbc-zap-kind")
            val onChainTxId = insertTransaction(txKind = TxKind.ON_CHAIN, invoice = "lnbc-onchain-kind")
            val nullInvoiceTxId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = null)
            val primalTxId = insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = "lnbc-primal-single",
                walletType = WalletType.PRIMAL,
            )

            processor.enrichTransaction(zapTxId) shouldBe false
            processor.enrichTransaction(onChainTxId) shouldBe false
            processor.enrichTransaction(nullInvoiceTxId) shouldBe false
            processor.enrichTransaction(primalTxId) shouldBe false
            fakeEventRepository.getZapRequestsCallCount shouldBe 0
        }

    @Test
    fun enrichTransactionApiFailureReturnsFalseNoIncrement() =
        runTest {
            val invoice = "lnbc-single-api-fail"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            fakeEventRepository.shouldFail = true

            val result = processor.enrichTransaction(txId)

            result shouldBe false
            val tracker = database.zapEnrichmentTracker().findByTransactionId(txId)!!
            tracker.attempts shouldBe 0
        }

    @Test
    fun enrichTransactionNotFoundReturnsFalse() =
        runTest {
            val result = processor.enrichTransaction("non-existent-tx-id")

            result shouldBe false
            fakeEventRepository.getZapRequestsCallCount shouldBe 0
        }

    @Test
    fun enrichTransactionRejectsPreCutoffSparkTransaction() =
        runTest {
            val invoice = "lnbc-single-old-spark"
            val txId = insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = invoice,
                walletType = WalletType.SPARK,
                createdAt = ENRICHMENT_CUTOFF_EPOCH_SECONDS - 1,
            )
            val zapRequest = buildZapRequestEvent()
            fakeEventRepository.zapRequestsToReturn = mapOf(invoice to zapRequest)

            val result = processor.enrichTransaction(txId)

            result shouldBe false
            fakeEventRepository.getZapRequestsCallCount shouldBe 0
        }

    @Test
    fun enrichTransactionAcceptsPreCutoffNwcTransaction() =
        runTest {
            val invoice = "lnbc-single-old-nwc"
            val txId = insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = invoice,
                walletType = WalletType.NWC,
                createdAt = ENRICHMENT_CUTOFF_EPOCH_SECONDS - 1,
            )
            val zapRequest = buildZapRequestEvent()
            fakeEventRepository.zapRequestsToReturn = mapOf(invoice to zapRequest)

            val result = processor.enrichTransaction(txId)

            result shouldBe true
            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.txKind shouldBe TxKind.ZAP
        }

    // endregion

    // region Attempt history tests

    @Test
    fun batchNetworkErrorRecordsNetworkErrorHistory() =
        runTest {
            val invoice = "lnbc-history-network"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            fakeEventRepository.shouldFail = true

            processor.processEnrichment()

            val history = getAttemptHistory(txId)
            history shouldHaveSize 1
            history.first().verdict shouldBe EnrichmentAttemptVerdict.NETWORK_ERROR
            history.first().attempt shouldBe 0
            history.first().detail shouldBe null
        }

    @Test
    fun batchMissRecordsNoZapRequestHistory() =
        runTest {
            val invoice = "lnbc-history-miss"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            fakeEventRepository.zapRequestsToReturn = emptyMap()

            processor.processEnrichment()

            val history = getAttemptHistory(txId)
            history shouldHaveSize 1
            history.first().verdict shouldBe EnrichmentAttemptVerdict.NO_ZAP_REQUEST
            history.first().detail shouldBe null
        }

    @Test
    fun malformedZapRequestRecordsVerdictWithDetail() =
        runTest {
            val invoice = "lnbc-history-malformed"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            // Zap request with no entity tags (no "e" or "a" tags) → toNostrEntity() returns null
            val malformedZapRequest = NostrEvent(
                id = "malformed-zap-req",
                pubKey = "sender",
                createdAt = 1000L,
                kind = 9734,
                tags = emptyList(),
                content = "",
                sig = "fake-sig",
            )
            fakeEventRepository.zapRequestsToReturn = mapOf(invoice to malformedZapRequest)

            processor.processEnrichment()

            val history = getAttemptHistory(txId)
            history shouldHaveSize 1
            history.first().verdict shouldBe EnrichmentAttemptVerdict.MALFORMED_ZAP_REQUEST
            history.first().detail shouldBe malformedZapRequest.encodeToJsonString()
        }

    @Test
    fun singleTxEnrichmentRecordsNoZapRequestHistory() =
        runTest {
            val invoice = "lnbc-history-single-miss"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            fakeEventRepository.zapRequestsToReturn = emptyMap()

            processor.enrichTransaction(txId)

            val history = getAttemptHistory(txId)
            history shouldHaveSize 1
            history.first().verdict shouldBe EnrichmentAttemptVerdict.NO_ZAP_REQUEST
        }

    @Test
    fun singleTxApiFailureRecordsNetworkErrorHistory() =
        runTest {
            val invoice = "lnbc-history-single-api-fail"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            fakeEventRepository.shouldFail = true

            processor.enrichTransaction(txId)

            val history = getAttemptHistory(txId)
            history shouldHaveSize 1
            history.first().verdict shouldBe EnrichmentAttemptVerdict.NETWORK_ERROR
        }

    @Test
    fun multipleAttemptsAccumulateHistory() =
        runTest {
            val invoice = "lnbc-history-accumulate"
            val txId = insertTransaction(txKind = TxKind.LIGHTNING, invoice = invoice)
            fakeEventRepository.zapRequestsToReturn = emptyMap()

            // First attempt
            processor.processEnrichment()
            // Backdate for second attempt
            backdateTracker(txId)
            processor.processEnrichment()

            val history = getAttemptHistory(txId)
            history shouldHaveSize 2
            history[0].attempt shouldBe 1
            history[1].attempt shouldBe 2
        }

    // endregion

    // region isEligibleForZapEnrichment tests

    @Test
    fun isEligibleReturnsFalseForPreCutoffSparkTransaction() =
        runTest {
            val txId = insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = "lnbc-elig-old-spark",
                walletType = WalletType.SPARK,
                createdAt = ENRICHMENT_CUTOFF_EPOCH_SECONDS - 1,
            )
            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.isEligibleForZapEnrichment() shouldBe false
        }

    @Test
    fun isEligibleReturnsTrueForPostCutoffSparkTransaction() =
        runTest {
            val txId = insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = "lnbc-elig-new-spark",
                walletType = WalletType.SPARK,
                createdAt = ENRICHMENT_CUTOFF_EPOCH_SECONDS + 1,
            )
            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.isEligibleForZapEnrichment() shouldBe true
        }

    @Test
    fun isEligibleReturnsTrueForPreCutoffNwcTransaction() =
        runTest {
            val txId = insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = "lnbc-elig-old-nwc",
                walletType = WalletType.NWC,
                createdAt = ENRICHMENT_CUTOFF_EPOCH_SECONDS - 1,
            )
            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.isEligibleForZapEnrichment() shouldBe true
        }

    @Test
    fun isEligibleReturnsFalseForPrimalTransaction() =
        runTest {
            val txId = insertTransaction(
                txKind = TxKind.LIGHTNING,
                invoice = "lnbc-elig-primal",
                walletType = WalletType.PRIMAL,
                createdAt = ENRICHMENT_CUTOFF_EPOCH_SECONDS + 1,
            )
            val tx = database.walletTransactions().findTransactionById(txId)!!
            tx.isEligibleForZapEnrichment() shouldBe false
        }

    // endregion

    // region Helpers

    private suspend fun getAttemptHistory(transactionId: String): List<EnrichmentAttemptEntry> {
        val tracker = database.zapEnrichmentTracker().findByTransactionId(transactionId)
            ?: return emptyList()
        val raw = tracker.attemptHistory
        if (raw.isBlank()) return emptyList()
        return "[$raw]".decodeFromJsonStringOrNull<List<EnrichmentAttemptEntry>>() ?: emptyList()
    }

    private suspend fun allPendingTrackers(): List<ZapEnrichmentTracker> {
        return database.zapEnrichmentTracker().findEligiblePending(
            maxAttempts = 5,
            threshold1 = Long.MAX_VALUE,
            threshold2 = Long.MAX_VALUE,
            threshold3 = Long.MAX_VALUE,
            limit = 100,
        )
    }

    private suspend fun backdateTracker(transactionId: String) {
        database.zapEnrichmentTracker().updateLastAttemptAt(transactionId, lastAttemptAt = 0)
    }

    private suspend fun allTrackers(): List<ZapEnrichmentTracker> {
        return database.zapEnrichmentTracker().findAll()
    }

    private suspend fun insertTransaction(
        txKind: TxKind,
        invoice: String?,
        walletType: WalletType = WalletType.SPARK,
        txType: TxType = TxType.DEPOSIT,
        createdAt: Long = ENRICHMENT_CUTOFF_EPOCH_SECONDS + 1000,
    ): String {
        val txId = UUID.randomUUID().toString()
        database.walletTransactions().upsertAll(
            listOf(
                WalletTransactionData(
                    transactionId = txId,
                    walletId = "wallet-1",
                    walletType = walletType,
                    type = txType,
                    state = TxState.SUCCEEDED,
                    createdAt = createdAt,
                    updatedAt = createdAt,
                    completedAt = createdAt.asEncryptable(),
                    userId = "user-1",
                    note = "test note".asEncryptable(),
                    invoice = invoice,
                    amountInBtc = 0.001.asEncryptable(),
                    totalFeeInBtc = null,
                    otherUserId = null,
                    zappedEntity = null,
                    zappedByUserId = null,
                    txKind = txKind,
                    onChainAddress = null,
                    onChainTxId = null,
                    preimage = null,
                    paymentHash = null,
                    amountInUsd = null,
                    exchangeRate = null,
                    otherLightningAddress = null,
                ),
            ),
        )
        return txId
    }

    private fun buildZapRequestEvent(
        pubKey: String = "sender-pubkey",
        profileId: String = "receiver-pubkey",
        eventId: String = "zapped-event-id",
        content: String = "Great post!",
    ): NostrEvent =
        NostrEvent(
            id = "zap-req-${UUID.randomUUID()}",
            pubKey = pubKey,
            createdAt = 1000L,
            kind = 9734,
            tags = listOf(
                buildJsonArray {
                    add("e")
                    add(eventId)
                },
                buildJsonArray {
                    add("p")
                    add(profileId)
                },
            ),
            content = content,
            sig = "fake-sig",
        )

    // endregion

    // region Fakes

    private class FakeEventRepository : EventRepository {
        var getZapRequestsCallCount = 0
        var shouldFail = false
        var zapRequestsToReturn: Map<String, NostrEvent> = emptyMap()

        override suspend fun getZapRequests(invoices: List<String>): Result<Map<String, NostrEvent>> {
            getZapRequestsCallCount++
            if (shouldFail) {
                return Result.failure(RuntimeException("API failure"))
            }
            val filtered = zapRequestsToReturn.filterKeys { it in invoices }
            return Result.success(filtered)
        }

        override fun pagedEventZaps(
            userId: String,
            eventId: String,
            articleATag: String?,
        ): Flow<PagingData<EventZap>> = throw NotImplementedError()

        override suspend fun observeZapsByEventId(eventId: String): Flow<List<EventZap>> = throw NotImplementedError()

        override fun observeEventStats(eventIds: List<String>): Flow<List<NostrEventStats>> =
            throw NotImplementedError()

        override fun observeUserEventStatus(eventIds: List<String>, userId: String): Flow<List<NostrEventUserStats>> =
            throw NotImplementedError()

        override suspend fun fetchEventActions(eventId: String, kind: Int): List<NostrEventAction> =
            throw NotImplementedError()

        override suspend fun fetchEventZaps(
            userId: String,
            eventId: String,
            limit: Int,
        ) = throw NotImplementedError()

        override suspend fun fetchReplaceableEvent(naddr: Naddr): Result<Unit> = throw NotImplementedError()

        override suspend fun fetchReplaceableEvents(naddrs: List<Naddr>): Result<Unit> = throw NotImplementedError()

        override suspend fun saveZapRequest(invoice: String, zapRequestEvent: NostrEvent) = throw NotImplementedError()

        override suspend fun deleteZapRequest(invoice: String) = throw NotImplementedError()
    }

    private class FakeProfileRepository : ProfileRepository {
        var fetchProfilesCallCount = 0
        var shouldFail = false

        override suspend fun fetchProfiles(profileIds: List<String>): List<ProfileData> {
            fetchProfilesCallCount++
            if (shouldFail) throw RuntimeException("Profile fetch failure")
            return emptyList()
        }

        override suspend fun fetchProfileId(primalName: String): String? = null

        override suspend fun findProfileDataOrNull(profileId: String): ProfileData? = null

        override suspend fun findProfileDataByLightningAddress(lightningAddress: String): ProfileData? = null

        override suspend fun findProfileData(profileIds: List<String>): List<ProfileData> = emptyList()

        override suspend fun findProfileStats(profileIds: List<String>): List<ProfileStats> = emptyList()

        override fun observeProfileData(profileId: String): Flow<ProfileData> = throw NotImplementedError()

        override fun observeProfileData(profileIds: List<String>): Flow<List<ProfileData>> = throw NotImplementedError()

        override fun observeProfileStats(profileId: String): Flow<ProfileStats?> = throw NotImplementedError()

        override suspend fun fetchProfile(profileId: String): ProfileData? = null

        override suspend fun fetchMissingProfiles(profileIds: List<String>): Result<List<ProfileData>> =
            Result.success(emptyList())

        override suspend fun fetchUserProfileFollowedBy(
            profileId: String,
            userId: String,
            limit: Int,
        ): List<ProfileData> = emptyList()

        override suspend fun isUserFollowing(userId: String, targetUserId: String): Boolean = false

        override suspend fun fetchFollowers(profileId: String): List<UserProfileSearchItem> = emptyList()

        override suspend fun fetchFollowing(profileId: String): List<UserProfileSearchItem> = emptyList()

        override suspend fun reportAbuse(
            userId: String,
            reportType: ReportType,
            profileId: String,
            eventId: String?,
            articleId: String?,
        ) = throw NotImplementedError()
    }

    // endregion
}
