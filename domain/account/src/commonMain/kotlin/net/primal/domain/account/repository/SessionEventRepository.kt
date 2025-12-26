package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.account.model.SessionEventUserChoice
import net.primal.domain.account.model.UserChoice
import net.primal.domain.nostr.cryptography.NostrKeyPair

interface SessionEventRepository {
    fun observeEventsPendingUserActionForRemoteSigner(signerPubKey: String): Flow<List<SessionEvent>>

    fun observeCompletedEventsForRemoteSession(sessionId: String): Flow<List<SessionEvent>>

    fun observeEventsPendingUserActionForLocalApp(appIdentifier: String): Flow<List<SessionEvent>>

    fun observeCompletedEventsForLocalSession(sessionId: String): Flow<List<SessionEvent>>

    fun observeRemoteEvent(eventId: String): Flow<SessionEvent?>

    fun observeLocalEvent(eventId: String): Flow<SessionEvent?>

    suspend fun notifyMissedNostrEvents(signerKeyPair: NostrKeyPair, eventIds: List<String>): Result<Unit>

    suspend fun respondToRemoteEvent(eventId: String, userChoice: UserChoice): Result<Unit>

    suspend fun respondToRemoteEvents(userChoices: List<SessionEventUserChoice>): Result<Unit>
}
