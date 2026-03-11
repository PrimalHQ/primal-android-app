package net.primal.data.repository.mappers.remote

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.toLong
import net.primal.data.local.dao.polls.PollData
import net.primal.data.local.dao.polls.PollOption
import net.primal.data.local.dao.polls.PollType
import net.primal.data.local.dao.polls.PollVoteData
import net.primal.data.remote.model.ContentPrimalPollOptionStats
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.findFirstBolt11
import net.primal.domain.nostr.findFirstClosedAt
import net.primal.domain.nostr.findFirstEndsAt
import net.primal.domain.nostr.findFirstEventId
import net.primal.domain.nostr.findFirstUnmarkedPubkey
import net.primal.domain.nostr.findFirstValueMaximum
import net.primal.domain.nostr.findFirstValueMinimum
import net.primal.domain.nostr.getTagValueOrNull
import net.primal.domain.nostr.isOptionTag
import net.primal.domain.nostr.isPollOptionTag
import net.primal.domain.nostr.isResponseTag
import net.primal.domain.nostr.utils.LnInvoiceUtils

fun List<NostrEvent>.mapNotNullAsPollDataPO(): List<PollData> {
    return filter { it.kind == NostrEventKind.Poll.value || it.kind == NostrEventKind.ZapPoll.value }
        .map { it.asPollData() }
}

fun List<NostrEvent>.mapAsPollResponseVotes(): List<PollVoteData> {
    return filter { it.kind == NostrEventKind.PollResponse.value }
        .flatMap { event ->
            val pollId = event.tags.findFirstEventId() ?: return@flatMap emptyList()
            event.tags
                .filter { it.isResponseTag() }
                .map { tag ->
                    PollVoteData(
                        eventId = event.id,
                        postId = pollId,
                        optionId = tag.getTagValueOrNull() ?: "",
                        voterId = event.pubKey,
                        amountInSats = null,
                        createdAt = event.createdAt,
                    )
                }
        }
}

fun List<NostrEvent>.mapAsZapPollVotes(): List<PollVoteData> {
    return filter { it.kind == NostrEventKind.Zap.value }
        .mapNotNull { zapReceipt ->
            val zapRequest = zapReceipt.extractZapRequestOrNull() ?: return@mapNotNull null

            val pollOptionId = zapRequest.tags
                .firstOrNull { it.isPollOptionTag() }
                ?.getTagValueOrNull()
                ?: return@mapNotNull null

            val pollId = zapRequest.tags.findFirstEventId() ?: return@mapNotNull null

            val amountInSats = zapReceipt.tags.findFirstBolt11()
                ?.let(LnInvoiceUtils::getAmountInSatsOrNull)
                ?.toLong()
                ?: return@mapNotNull null

            PollVoteData(
                eventId = zapReceipt.id,
                postId = pollId,
                optionId = pollOptionId,
                voterId = zapRequest.pubKey,
                amountInSats = amountInSats,
                zapComment = zapRequest.content.takeIf { it.isNotBlank() },
                createdAt = zapReceipt.createdAt,
            )
        }
}

internal typealias PollStatsMap = Map<String, Map<String, ContentPrimalPollOptionStats>>

fun PrimalEvent?.parseAndMapPrimalPollStats(): PollStatsMap {
    return this?.content?.decodeFromJsonStringOrNull<PollStatsMap>() ?: emptyMap()
}

fun List<PollData>.applyPollStats(statsMap: PollStatsMap): List<PollData> {
    if (statsMap.isEmpty()) return this
    return map { poll ->
        val optionStatsMap = statsMap[poll.postId] ?: return@map poll
        poll.copy(
            options = poll.options.map { option ->
                val optionStats = optionStatsMap[option.id]
                if (optionStats != null) {
                    option.copy(
                        voteCount = optionStats.votes,
                        satsZapped = optionStats.satsZapped,
                    )
                } else {
                    option
                }
            },
        )
    }
}

private fun NostrEvent.asPollData(): PollData {
    val isZapPoll = this.kind == NostrEventKind.ZapPoll.value
    return PollData(
        postId = this.id,
        authorId = this.pubKey,
        zapRecipientId = if (isZapPoll) {
            this.tags.findFirstUnmarkedPubkey() ?: this.pubKey
        } else {
            null
        },
        pollType = if (isZapPoll) PollType.Zap else PollType.User,
        endsAt = if (isZapPoll) {
            tags.findFirstClosedAt()?.toLongOrNull()
        } else {
            tags.findFirstEndsAt()?.toLongOrNull()
        },
        valueMinimum = if (isZapPoll) tags.findFirstValueMinimum()?.toLongOrNull() else null,
        valueMaximum = if (isZapPoll) tags.findFirstValueMaximum()?.toLongOrNull() else null,
        options = if (isZapPoll) parsePollOptions { it.isPollOptionTag() } else parsePollOptions { it.isOptionTag() },
    )
}

private fun NostrEvent.parsePollOptions(tagFilter: (JsonArray) -> Boolean): List<PollOption> {
    return tags
        .filter { tagFilter(it) && it.size >= 3 }
        .map { tag ->
            PollOption(
                id = tag[1].jsonPrimitive.content,
                label = tag[2].jsonPrimitive.content,
            )
        }
}
