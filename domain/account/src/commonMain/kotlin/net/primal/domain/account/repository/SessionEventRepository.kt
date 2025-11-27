package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.account.model.UserChoice
import net.primal.domain.nostr.cryptography.NostrKeyPair

interface SessionEventRepository {
    fun observeEventsPendingUserAction(signerPubKey: String): Flow<List<SessionEvent>>

    fun observeEventsForSession(sessionId: String): Flow<List<SessionEvent>>

    fun observeEvent(eventId: String): Flow<SessionEvent?>

    suspend fun processMissedEvents(signerKeyPair: NostrKeyPair, eventIds: List<String>): Result<Unit>

    suspend fun respondToEvent(eventId: String, userChoice: UserChoice): Result<Unit>

    suspend fun respondToEvents(eventIdToUserChoice: List<Pair<String, UserChoice>>): Result<Unit>
}
