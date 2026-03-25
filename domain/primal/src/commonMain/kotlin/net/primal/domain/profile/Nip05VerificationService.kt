package net.primal.domain.profile

import kotlinx.coroutines.flow.Flow

interface Nip05VerificationService {
    suspend fun getStatus(pubkey: String): Nip05VerificationStatus?
    suspend fun getStatuses(pubkeys: List<String>): Map<String, Nip05VerificationStatus?>
    fun observeStatus(pubkey: String): Flow<Nip05VerificationStatus?>
    suspend fun verifyIfNeeded(pubkey: String, internetIdentifier: String)
    suspend fun verifyEagerly(pubkey: String, internetIdentifier: String)
}
