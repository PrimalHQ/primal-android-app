package net.primal.data.repository.mappers.remote

import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.local.dao.streams.StreamChatMessageData
import net.primal.data.local.dao.streams.StreamData
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.findAllHashtags
import net.primal.domain.nostr.findFirstATag
import net.primal.domain.nostr.findFirstClient
import net.primal.domain.nostr.findFirstCurrentParticipants
import net.primal.domain.nostr.findFirstEnds
import net.primal.domain.nostr.findFirstHostPubkey
import net.primal.domain.nostr.findFirstIdentifier
import net.primal.domain.nostr.findFirstImage
import net.primal.domain.nostr.findFirstRecording
import net.primal.domain.nostr.findFirstStarts
import net.primal.domain.nostr.findFirstStatus
import net.primal.domain.nostr.findFirstStreaming
import net.primal.domain.nostr.findFirstSummary
import net.primal.domain.nostr.findFirstTitle
import net.primal.domain.nostr.findFirstTotalParticipants
import net.primal.domain.nostr.serialization.toNostrJsonObject
import net.primal.domain.streams.StreamStatus

fun List<NostrEvent>.mapNotNullAsStreamDataPO(): List<StreamData> {
    return this.mapNotNull { it.asStreamData() }
}

fun NostrEvent.asStreamData(): StreamData? {
    if (this.kind != NostrEventKind.LiveActivity.value) return null

    val dTag = this.tags.findFirstIdentifier() ?: return null
    val mainHostId = this.tags.findFirstHostPubkey() ?: this.pubKey
    val eventAuthorId = this.pubKey

    return StreamData(
        aTag = "${this.kind}:$eventAuthorId:$dTag",
        eventId = id,
        eventAuthorId = eventAuthorId,
        mainHostId = mainHostId,
        dTag = dTag,
        title = this.tags.findFirstTitle(),
        summary = this.tags.findFirstSummary(),
        imageUrl = this.tags.findFirstImage(),
        hashtags = this.tags.findAllHashtags(),
        streamingUrl = this.tags.findFirstStreaming(),
        recordingUrl = this.tags.findFirstRecording(),
        status = StreamStatus.fromString(this.tags.findFirstStatus()),
        startsAt = this.tags.findFirstStarts()?.toLongOrNull(),
        endsAt = this.tags.findFirstEnds()?.toLongOrNull(),
        currentParticipants = this.tags.findFirstCurrentParticipants()?.toIntOrNull(),
        totalParticipants = this.tags.findFirstTotalParticipants()?.toIntOrNull(),
        raw = this.toNostrJsonObject().encodeToJsonString(),
        createdAt = this.createdAt,
    )
}

fun NostrEvent.asChatMessageDataDO(): StreamChatMessageData? {
    if (this.kind != NostrEventKind.ChatMessage.value) return null

    val streamATag = this.tags.findFirstATag() ?: return null

    return StreamChatMessageData(
        messageId = this.id,
        streamATag = streamATag,
        authorId = this.pubKey,
        createdAt = this.createdAt,
        content = this.content,
        raw = this.encodeToJsonString(),
        client = this.tags.findFirstClient(),
    )
}
