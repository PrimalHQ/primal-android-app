package net.primal.data.repository.nip05

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import net.primal.data.local.dao.profiles.Nip05VerificationData
import net.primal.data.local.dao.profiles.Nip05VerificationDataDao
import net.primal.domain.profile.Nip05VerificationService
import net.primal.domain.profile.Nip05VerificationStatus

class Nip05VerificationServiceImpl(
    private val httpClient: HttpClient,
    private val verificationDao: Nip05VerificationDataDao,
) : Nip05VerificationService {
    companion object {
        private const val TAG = "Nip05Verification"
        const val MAX_CONCURRENT_VERIFICATIONS = 5
        val VERIFIED_TTL = 7.days
        val FAILED_TTL = 30.minutes
        val ERROR_TTL = 5.minutes
    }

    private val mutex = Mutex()
    private val inFlightVerifications = mutableSetOf<String>()
    private val verificationSemaphore = Semaphore(MAX_CONCURRENT_VERIFICATIONS)

    override suspend fun getStatus(pubkey: String): Nip05VerificationStatus? = verificationDao.find(pubkey)?.status

    override suspend fun getStatuses(pubkeys: List<String>): Map<String, Nip05VerificationStatus?> {
        if (pubkeys.isEmpty()) return emptyMap()
        val results = verificationDao.findAll(pubkeys)
        val statusByPubkey = results.associate { it.ownerId to it.status }
        return pubkeys.associateWith { statusByPubkey[it] }
    }

    override fun observeStatus(pubkey: String): Flow<Nip05VerificationStatus?> =
        verificationDao.observe(pubkey).map { it?.status }

    override suspend fun verifyIfNeeded(pubkey: String, internetIdentifier: String) {
        val cached = verificationDao.find(pubkey)
        if (cached != null && !isExpired(cached) && cached.verifiedAddress == internetIdentifier) {
            Napier.d(tag = TAG) { "Skipping verification for $internetIdentifier (cached=${cached.status})" }
            return
        }
        Napier.d(tag = TAG) {
            "Starting verification for $internetIdentifier (pubkey=${pubkey.take(8)}..., " +
                "cached=${cached?.status}, expired=${cached?.let { isExpired(it) }}, " +
                "addressChanged=${cached?.verifiedAddress != internetIdentifier})"
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
                verificationDao.upsert(
                    Nip05VerificationData(
                        ownerId = pubkey,
                        status = result.status,
                        lastCheckedAt = currentEpochSeconds(),
                        verifiedAddress = internetIdentifier,
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
        val url = "https://$domain/.well-known/nostr.json"

        return try {
            Napier.d(tag = TAG) { "Fetching $url?name=$localPart" }
            val response = httpClient.get(url) {
                parameter("name", localPart)
            }
            if (!response.status.isSuccess()) {
                Napier.d(tag = TAG) { "HTTP ${response.status} for $url" }
                return VerificationResult(status = Nip05VerificationStatus.ERROR)
            }
            val body = response.body<Nip05WellKnownResponse>()
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
