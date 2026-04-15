package net.primal.data.repository.nip05

import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import net.primal.data.local.dao.profiles.Nip05VerificationData
import net.primal.data.local.dao.profiles.Nip05VerificationDataDao
import net.primal.domain.profile.Nip05VerificationService
import net.primal.domain.profile.Nip05VerificationStatus

class Nip05VerificationServiceImpl(
    private val nip05HttpClient: Nip05HttpClient,
    private val verificationDao: Nip05VerificationDataDao,
) : Nip05VerificationService {
    companion object {
        private const val TAG = "Nip05Verification"
        const val MAX_CONCURRENT_VERIFICATIONS = 5
        val VERIFIED_TTL = 7.days
        val FAILED_TTL = 30.minutes
        val ERROR_TTL = 5.minutes
        private val VALID_DOMAIN = Regex(
            "^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?)+$",
        )
    }

    private data class CacheEntry(
        val status: Nip05VerificationStatus,
        val verifiedAddress: String,
        val lastCheckedAt: Long,
    )

    private val statusCache = MutableStateFlow<Map<String, CacheEntry>>(emptyMap())

    private val mutex = Mutex()
    private val inFlightVerifications = mutableSetOf<String>()
    private val verificationSemaphore = Semaphore(MAX_CONCURRENT_VERIFICATIONS)

    override suspend fun getStatus(pubkey: String): Nip05VerificationStatus? {
        statusCache.value[pubkey]?.let { return it.status }
        return verificationDao.find(pubkey)?.also { data ->
            updateCache(
                pubkey,
                CacheEntry(
                    status = data.status,
                    verifiedAddress = data.verifiedAddress,
                    lastCheckedAt = data.lastCheckedAt,
                ),
            )
        }?.status
    }

    override suspend fun getStatuses(pubkeys: List<String>): Map<String, Nip05VerificationStatus?> {
        if (pubkeys.isEmpty()) return emptyMap()
        val cache = statusCache.value
        val cached = pubkeys.filter { it in cache }.associateWith { cache[it]?.status }
        val missing = pubkeys - cached.keys
        if (missing.isEmpty()) return cached
        val dbResults = verificationDao.findAll(missing)
        val dbMap = dbResults.associate { it.ownerId to it.status }
        updateCacheBatch(
            dbResults.associate { data ->
                data.ownerId to CacheEntry(
                    status = data.status,
                    verifiedAddress = data.verifiedAddress,
                    lastCheckedAt = data.lastCheckedAt,
                )
            },
        )
        return pubkeys.associateWith { cached[it] ?: dbMap[it] }
    }

    override fun observeStatus(pubkey: String): Flow<Nip05VerificationStatus?> =
        statusCache.map { it[pubkey]?.status }.distinctUntilChanged()

    override suspend fun verifyIfNeeded(pubkey: String, internetIdentifier: String) {
        // Tier 1: in-memory cache (no IO)
        val memCached = statusCache.value[pubkey]
        if (memCached != null && !isExpired(memCached) && memCached.verifiedAddress == internetIdentifier) {
            Napier.d(tag = TAG) { "Skipping $internetIdentifier — in-memory cache hit (${memCached.status})" }
            return
        }

        // Tier 2: Room cache (single query, populates in-memory cache)
        val dbCached = verificationDao.find(pubkey)
        if (dbCached != null) {
            updateCache(
                pubkey,
                CacheEntry(
                    status = dbCached.status,
                    verifiedAddress = dbCached.verifiedAddress,
                    lastCheckedAt = dbCached.lastCheckedAt,
                ),
            )
            if (!isExpired(dbCached) && dbCached.verifiedAddress == internetIdentifier) {
                Napier.d(tag = TAG) { "Skipping $internetIdentifier — DB cache hit (${dbCached.status})" }
                return
            }
        }

        launchVerification(pubkey, internetIdentifier)
    }

    override suspend fun verifyEagerly(pubkey: String, internetIdentifier: String) {
        launchVerification(pubkey, internetIdentifier)
    }

    private suspend fun launchVerification(pubkey: String, internetIdentifier: String) {
        val acquired = mutex.withLock {
            if (pubkey in inFlightVerifications) {
                false
            } else {
                inFlightVerifications.add(pubkey)
                true
            }
        }
        if (!acquired) {
            Napier.d(tag = TAG) { "Skipping $internetIdentifier — already in-flight" }
            return
        }

        try {
            verificationSemaphore.withPermit {
                val result = performVerification(pubkey, internetIdentifier)
                Napier.d(tag = TAG) { "Verification result for $internetIdentifier: ${result.status}" }

                // Don't overwrite VERIFIED or FAILED with ERROR.
                // Stale verified/failed data is better than flipping to error when offline.
                val cached = verificationDao.find(pubkey)
                if (result.status == Nip05VerificationStatus.ERROR &&
                    cached?.status in setOf(
                        Nip05VerificationStatus.VERIFIED,
                        Nip05VerificationStatus.FAILED,
                    ) &&
                    cached?.verifiedAddress == internetIdentifier
                ) {
                    Napier.d(
                        tag = TAG,
                    ) { "Keeping cached ${cached.status} for $internetIdentifier (not overwriting with ERROR)" }
                    return@withPermit
                }

                Napier.d(
                    tag = TAG,
                ) { "Persisting ${result.status} for $internetIdentifier (pubkey=${pubkey.take(8)}...)" }
                val now = currentEpochSeconds()
                verificationDao.upsert(
                    Nip05VerificationData(
                        ownerId = pubkey,
                        status = result.status,
                        lastCheckedAt = now,
                        verifiedAddress = internetIdentifier,
                    ),
                )
                updateCache(
                    pubkey,
                    CacheEntry(
                        status = result.status,
                        verifiedAddress = internetIdentifier,
                        lastCheckedAt = now,
                    ),
                )

                persistOptimisticVerifications(result.allNames, result.domain, pubkey)
            }
        } finally {
            mutex.withLock {
                inFlightVerifications.remove(pubkey)
            }
        }
    }

    private suspend fun persistOptimisticVerifications(
        allNames: Map<String, String>,
        domain: String,
        excludePubkey: String,
    ) {
        val otherEntries = allNames.filter { it.value != excludePubkey }
        if (otherEntries.isEmpty()) return

        val now = currentEpochSeconds()
        val optimisticInserts = otherEntries.map { (name, pubkey) ->
            Nip05VerificationData(
                ownerId = pubkey,
                status = Nip05VerificationStatus.VERIFIED,
                lastCheckedAt = now,
                verifiedAddress = "${name.lowercase()}@$domain",
            )
        }

        Napier.d(tag = TAG) { "Upserting ${optimisticInserts.size} optimistic verifications from $domain" }
        verificationDao.upsertAll(optimisticInserts)
        updateCacheBatch(
            optimisticInserts.associate { data ->
                data.ownerId to CacheEntry(
                    status = data.status,
                    verifiedAddress = data.verifiedAddress,
                    lastCheckedAt = data.lastCheckedAt,
                )
            },
        )
    }

    private data class VerificationResult(
        val status: Nip05VerificationStatus,
        val allNames: Map<String, String> = emptyMap(),
        val domain: String = "",
    )

    private suspend fun performVerification(pubkey: String, internetIdentifier: String): VerificationResult {
        val parts = internetIdentifier.split("@")
        if (parts.size != 2) {
            Napier.d(tag = TAG) { "Invalid identifier format: $internetIdentifier" }
            return VerificationResult(status = Nip05VerificationStatus.FAILED)
        }

        val localPart = parts[0]
        val domain = parts[1]

        if (!VALID_DOMAIN.matches(domain)) {
            Napier.d(tag = TAG) { "Invalid domain: $domain" }
            return VerificationResult(status = Nip05VerificationStatus.FAILED)
        }

        return try {
            val body = nip05HttpClient.fetchWellKnown(domain = domain, name = localPart)
                ?: return VerificationResult(status = Nip05VerificationStatus.ERROR)

            Napier.d(tag = TAG) { "Response names keys: ${body.names.keys} for localPart=$localPart" }
            val returnedPubkey = body.names.entries
                .firstOrNull { it.key.equals(localPart, ignoreCase = true) }
                ?.value
            Napier.d(tag = TAG) {
                "Pubkey match: returned=${returnedPubkey?.take(8)}..., " +
                    "expected=${pubkey.take(8)}..., match=${returnedPubkey == pubkey}"
            }
            val status = if (returnedPubkey == pubkey) {
                Nip05VerificationStatus.VERIFIED
            } else {
                Nip05VerificationStatus.FAILED
            }
            VerificationResult(status = status, allNames = body.names, domain = domain)
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "Exception verifying $internetIdentifier" }
            VerificationResult(status = Nip05VerificationStatus.ERROR)
        }
    }

    private fun updateCache(pubkey: String, entry: CacheEntry) {
        statusCache.update { it + (pubkey to entry) }
    }

    private fun updateCacheBatch(entries: Map<String, CacheEntry>) {
        statusCache.update { it + entries }
    }

    private fun isExpired(entry: CacheEntry): Boolean {
        val ageSeconds = currentEpochSeconds() - entry.lastCheckedAt
        val ttlSeconds = when (entry.status) {
            Nip05VerificationStatus.VERIFIED -> VERIFIED_TTL
            Nip05VerificationStatus.FAILED -> FAILED_TTL
            Nip05VerificationStatus.ERROR -> ERROR_TTL
        }.inWholeSeconds
        return ageSeconds > ttlSeconds
    }

    private fun isExpired(data: Nip05VerificationData): Boolean {
        val ageSeconds = currentEpochSeconds() - data.lastCheckedAt
        val ttlSeconds = when (data.status) {
            Nip05VerificationStatus.VERIFIED -> VERIFIED_TTL
            Nip05VerificationStatus.FAILED -> FAILED_TTL
            Nip05VerificationStatus.ERROR -> ERROR_TTL
        }.inWholeSeconds
        return ageSeconds > ttlSeconds
    }

    private fun currentEpochSeconds(): Long = Clock.System.now().epochSeconds
}
