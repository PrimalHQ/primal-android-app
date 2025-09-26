package net.primal.domain.streams

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.nostr.Naddr

interface StreamRepository {

    fun observeLiveStreamsByMainHostId(mainHostId: String): Flow<List<Stream>>

    suspend fun findWhoIsLive(mainHostIds: List<String>): Set<String>

    fun observeStream(aTag: String): Flow<Stream?>

    suspend fun getStream(aTag: String): Result<Stream>

    suspend fun getStaleStreamNaddrs(): List<Naddr>

    /**
     * Starts a live stream subscription and begins collecting events in a background [Job].
     *
     * This suspending function does not return until the upstream flow has emitted at least one
     * item **or** failed. After that first emission (or error), it returns the [Job] that keeps
     * collecting.
     *
     * @param naddr Address of the live stream to subscribe to.
     * @param userId ID of the user initiating the subscription.
     * @param streamContentModerationMode Moderation level to apply to the stream.
     * @return A [Job] that owns the continuous collection of live events.
     */
    suspend fun awaitLiveStreamSubscriptionStart(
        naddr: Naddr,
        userId: String,
        streamContentModerationMode: StreamContentModerationMode,
    ): Job

    suspend fun startLiveEventsFromFollowsSubscription(userId: String)

    fun observeLiveEventsFromFollows(userId: String): Flow<List<Stream>>

    suspend fun findStreamNaddr(hostPubkey: String, identifier: String): Result<Naddr>
}
