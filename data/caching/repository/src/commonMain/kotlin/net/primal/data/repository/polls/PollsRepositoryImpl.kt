package net.primal.data.repository.polls

import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.remote.api.polls.PollsApi
import net.primal.data.remote.api.polls.model.PollVotesRequestBody
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.mappers.local.asProfileDataDO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.findFirstZapAmount
import net.primal.domain.polls.PollOptionStats
import net.primal.domain.polls.PollVoteStats
import net.primal.domain.polls.PollVoter
import net.primal.domain.polls.PollsRepository

class PollsRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val pollsApi: PollsApi,
) : PollsRepository {

    override suspend fun fetchPollVotes(eventId: String): PollVoteStats =
        withContext(dispatcherProvider.io()) {
            val response = pollsApi.getPollVotes(
                body = PollVotesRequestBody(eventId = eventId, limit = 100),
            )

            val primalUserNames = response.primalUserNames.parseAndMapPrimalUserNames()
            val primalPremiumInfo = response.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
            val primalLegendProfiles = response.primalLegendProfiles.parseAndMapPrimalLegendProfiles()
            val cdnResources = response.cdnResources.flatMapNotNullAsCdnResource()
            val blossomServers = response.blossomServers.mapAsMapPubkeyToListOfBlossomServers()

            val profilesMap = response.profiles.mapAsProfileDataPO(
                cdnResources = cdnResources,
                primalUserNames = primalUserNames,
                primalPremiumInfo = primalPremiumInfo,
                primalLegendProfiles = primalLegendProfiles,
                blossomServers = blossomServers,
            ).associateBy { it.ownerId }

            val pollEvent = response.referencedEvents
                .mapNotNull { it.content.decodeFromJsonStringOrNull<NostrEvent>() }
                .firstOrNull {
                    it.kind == NostrEventKind.Poll.value || it.kind == NostrEventKind.ZapPoll.value
                }

            val isZapPoll = pollEvent?.kind == NostrEventKind.ZapPoll.value ||
                pollEvent?.tags?.any {
                    it.firstOrNull()?.jsonPrimitive?.content == "value_minimum" ||
                        it.firstOrNull()?.jsonPrimitive?.content == "value_maximum"
                } == true

            val optionTags = pollEvent?.tags
                ?.filter { it.firstOrNull()?.jsonPrimitive?.content == "option" }
                ?.mapNotNull { tag ->
                    val id = tag.getOrNull(1)?.jsonPrimitive?.content
                    val label = tag.getOrNull(2)?.jsonPrimitive?.content
                    if (id != null && label != null) id to label else null
                }
                ?: emptyList()

            val votesByOption = mutableMapOf<String, MutableList<VoteData>>()
            response.votes.forEach { voteEvent ->
                val optionId = voteEvent.extractPollOptionId()
                if (optionId != null) {
                    val sats = voteEvent.extractZapAmount()
                    val comment = if (isZapPoll) voteEvent.content.takeIf { it.isNotBlank() } else null
                    votesByOption.getOrPut(optionId) { mutableListOf() }
                        .add(VoteData(pubKey = voteEvent.pubKey, satsZapped = sats, zapComment = comment))
                }
            }

            val statsJson = response.pollStats?.content?.decodeFromJsonStringOrNull<JsonObject>()
            val voteCounts = statsJson?.get("options")?.jsonObject

            PollVoteStats(
                eventId = eventId,
                isZapPoll = isZapPoll,
                options = optionTags.map { (optionId, label) ->
                    val votes = votesByOption[optionId] ?: emptyList()
                    val optionStats = voteCounts?.get(optionId)?.jsonObject
                    PollOptionStats(
                        optionId = optionId,
                        optionTitle = label,
                        voteCount = optionStats?.get("votes")?.jsonPrimitive?.intOrNull ?: votes.size,
                        totalSats = optionStats?.get("sats")?.jsonPrimitive?.longOrNull
                            ?: votes.sumOf { it.satsZapped },
                        voters = votes.mapNotNull { vote ->
                            profilesMap[vote.pubKey]?.asProfileDataDO()?.let { profile ->
                                PollVoter(
                                    profile = profile,
                                    satsZapped = vote.satsZapped,
                                    zapComment = vote.zapComment,
                                )
                            }
                        },
                    )
                },
            )
        }

    private fun NostrEvent.extractPollOptionId(): String? {
        return when (kind) {
            NostrEventKind.ZapRequest.value, NostrEventKind.Zap.value ->
                tags.find { it.firstOrNull()?.jsonPrimitive?.content == "poll_option" }
                    ?.getOrNull(1)?.jsonPrimitive?.content

            NostrEventKind.PollResponse.value ->
                tags.find { it.firstOrNull()?.jsonPrimitive?.content == "response" }
                    ?.getOrNull(1)?.jsonPrimitive?.content

            else -> null
        }
    }

    private fun NostrEvent.extractZapAmount(): Long {
        return tags.findFirstZapAmount()?.toLongOrNull()?.let { it / 1000 } ?: 0
    }
}

private data class VoteData(val pubKey: String, val satsZapped: Long, val zapComment: String?)
