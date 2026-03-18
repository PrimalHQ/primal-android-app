package net.primal.android.editor

import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.editor.domain.PollOption
import net.primal.android.editor.domain.PollPublishRequest
import net.primal.android.editor.domain.asIMetaTag
import net.primal.android.networking.UserAgentProvider
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.domain.RelayKind
import net.primal.android.user.domain.mapToRelayDO
import net.primal.android.user.repository.RelayRepository
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.events.EventRelayHintsRepository
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asClientTag
import net.primal.domain.nostr.asEventTag
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.asReplaceableEventTag
import net.primal.domain.nostr.isPubKeyTag
import net.primal.domain.nostr.parseEventTags
import net.primal.domain.nostr.parseHashtagTags
import net.primal.domain.nostr.parsePubkeyTags
import net.primal.domain.nostr.parseReplaceableEventTags
import net.primal.domain.posts.FeedRepository
import net.primal.domain.publisher.PrimalPublishResult

class NotePublishHandler @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val nostrPublisher: NostrPublisher,
    private val eventRelayHintsRepository: EventRelayHintsRepository,
    private val feedRepository: FeedRepository,
    private val relayRepository: RelayRepository,
) {

    @Suppress("LongParameterList")
    private suspend fun prepareNote(
        content: String,
        attachments: List<NoteAttachment>,
        rootNoteNevent: Nevent?,
        rootArticleNaddr: Naddr?,
        rootHighlightNevent: Nevent?,
        replyToNoteNevent: Nevent?,
    ): PreparedNote {
        val noteContent = content.ensureWhitespaceBeforeUserTag()

        /* Event mentions from content */
        val mentionEventTags = noteContent.parseEventTags(marker = "mention")
        val mentionReplaceableEventTags = noteContent.parseReplaceableEventTags(marker = "mention")
        val allMentionedEventTags = mentionEventTags + mentionReplaceableEventTags

        /* Pubkey mentions from content and referenced pub keys */
        val mentionPubkeyTags = noteContent.parsePubkeyTags(marker = "mention").toSet()
        val referencedPubkeyTags = resolveReferencedPubkeyTags(
            rootNoteNevent = rootNoteNevent,
            rootArticleNaddr = rootArticleNaddr,
            rootHighlightNevent = rootHighlightNevent,
            replyToNoteNevent = replyToNoteNevent,
        )
        val allPubkeyTags = mentionPubkeyTags + referencedPubkeyTags

        /* Reply & Root tags */
        val replyEventTag = resolveReplyEventTag(
            rootNote = rootNoteNevent,
            replyTo = replyToNoteNevent,
            rootHighlight = rootHighlightNevent,
            rootArticle = rootArticleNaddr,
        )
        val rootTag = resolveRootEventTag(
            highlight = rootHighlightNevent,
            article = rootArticleNaddr,
            note = rootNoteNevent,
        )

        /* Hashtag tags */
        val hashtagTags = noteContent.parseHashtagTags().toSet()

        /* iMeta tags */
        val iMetaTags = attachments.filter { it.isMediaAttachment }.map { it.asIMetaTag() }

        /* Relay Hints */
        val allReferenceTags = (allMentionedEventTags + allPubkeyTags + replyEventTag + rootTag).filterNotNull().toSet()
        val relayHintsMap = withContext(dispatcherProvider.io()) {
            val tagNoteIds = allReferenceTags.map { it.get(index = 1).jsonPrimitive.content }
            val hints = eventRelayHintsRepository.findRelaysByIds(eventIds = tagNoteIds)
            hints.associate { it.eventId to it.relays.first() }
        }
        val allReferenceTagsWithRelays = allReferenceTags.insertRelayHints(relayHintsMap = relayHintsMap)

        /* Content */
        val refinedContent = noteContent.appendAttachmentUrls(attachments)

        /* Outbox relays */
        val outboxRelays = replyToNoteNevent?.eventId?.let { noteId ->
            relayHintsMap[noteId]?.let { relayUrl -> listOf(relayUrl) }
        } ?: emptyList()

        return PreparedNote(
            refinedContent = refinedContent,
            referenceTags = allReferenceTagsWithRelays,
            hashtagTags = hashtagTags,
            iMetaTags = iMetaTags,
            outboxRelays = outboxRelays,
        )
    }

    @Throws(NostrPublishException::class)
    suspend fun publishShortTextNote(
        userId: String,
        content: String,
        attachments: List<NoteAttachment> = emptyList(),
        rootNoteNevent: Nevent? = null,
        rootArticleNaddr: Naddr? = null,
        rootHighlightNevent: Nevent? = null,
        replyToNoteNevent: Nevent? = null,
    ): PrimalPublishResult {
        val prepared = prepareNote(
            content = content,
            attachments = attachments,
            rootNoteNevent = rootNoteNevent,
            rootArticleNaddr = rootArticleNaddr,
            rootHighlightNevent = rootHighlightNevent,
            replyToNoteNevent = replyToNoteNevent,
        )

        return withContext(dispatcherProvider.io()) {
            nostrPublisher.signPublishImportNostrEvent(
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = NostrEventKind.ShortTextNote.value,
                    tags = (prepared.referenceTags + prepared.hashtagTags + prepared.iMetaTags).toList() +
                        listOf(UserAgentProvider.CLIENT_NAME.asClientTag()),
                    content = prepared.refinedContent,
                ),
                outboxRelays = prepared.outboxRelays,
            )
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun publishPoll(
        userId: String,
        content: String,
        attachments: List<NoteAttachment> = emptyList(),
        pollRequest: PollPublishRequest,
        rootNoteNevent: Nevent? = null,
        rootArticleNaddr: Naddr? = null,
        rootHighlightNevent: Nevent? = null,
        replyToNoteNevent: Nevent? = null,
    ): PrimalPublishResult {
        val prepared = prepareNote(
            content = content,
            attachments = attachments,
            rootNoteNevent = rootNoteNevent,
            rootArticleNaddr = rootArticleNaddr,
            rootHighlightNevent = rootHighlightNevent,
            replyToNoteNevent = replyToNoteNevent,
        )

        val eventKind = if (pollRequest.isZapPoll) NostrEventKind.ZapPoll else NostrEventKind.Poll

        return withContext(dispatcherProvider.io()) {
            val writeRelayUrls = relayRepository.findRelays(userId, RelayKind.UserRelay)
                .map { it.mapToRelayDO() }
                .filter { it.write }
                .map { it.url }

            val pollTags = if (pollRequest.isZapPoll) {
                buildZapPollTags(
                    userId = userId,
                    choices = pollRequest.choices,
                    endsAt = pollRequest.endsAt,
                    minZapAmountInSats = pollRequest.minZapAmountInSats,
                    maxZapAmountInSats = pollRequest.maxZapAmountInSats,
                    writeRelayUrls = writeRelayUrls,
                )
            } else {
                buildUserPollTags(
                    choices = pollRequest.choices,
                    endsAt = pollRequest.endsAt,
                    writeRelayUrls = writeRelayUrls,
                )
            }

            nostrPublisher.signPublishImportNostrEvent(
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = eventKind.value,
                    tags = (prepared.referenceTags + prepared.hashtagTags + prepared.iMetaTags + pollTags).toList() +
                        listOf(UserAgentProvider.CLIENT_NAME.asClientTag()),
                    content = prepared.refinedContent,
                ),
                outboxRelays = prepared.outboxRelays,
            )
        }
    }

    private fun buildUserPollTags(
        choices: List<PollOption>,
        endsAt: Long,
        writeRelayUrls: List<String>,
    ): List<JsonArray> {
        val tags = mutableListOf<JsonArray>()

        choices.forEach { (id, label) ->
            tags.add(JsonArray(listOf(JsonPrimitive("option"), JsonPrimitive(id), JsonPrimitive(label))))
        }

        tags.add(JsonArray(listOf(JsonPrimitive("polltype"), JsonPrimitive("singlechoice"))))
        tags.add(JsonArray(listOf(JsonPrimitive("endsAt"), JsonPrimitive(endsAt.toString()))))

        writeRelayUrls.forEach { relayUrl ->
            tags.add(JsonArray(listOf(JsonPrimitive("relay"), JsonPrimitive(relayUrl))))
        }

        return tags
    }

    @Suppress("LongParameterList")
    private fun buildZapPollTags(
        userId: String,
        choices: List<PollOption>,
        endsAt: Long,
        minZapAmountInSats: Long?,
        maxZapAmountInSats: Long?,
        writeRelayUrls: List<String>,
    ): List<JsonArray> {
        val tags = mutableListOf<JsonArray>()

        choices.forEachIndexed { index, choice ->
            tags.add(
                JsonArray(
                    listOf(JsonPrimitive("poll_option"), JsonPrimitive(index.toString()), JsonPrimitive(choice.label)),
                ),
            )
        }

        val firstWriteRelayUrl = writeRelayUrls.firstOrNull()
        val pTag = if (firstWriteRelayUrl != null) {
            listOf(JsonPrimitive("p"), JsonPrimitive(userId), JsonPrimitive(firstWriteRelayUrl))
        } else {
            listOf(JsonPrimitive("p"), JsonPrimitive(userId))
        }
        tags.add(JsonArray(pTag))

        tags.add(JsonArray(listOf(JsonPrimitive("closed_at"), JsonPrimitive(endsAt.toString()))))

        if (minZapAmountInSats != null) {
            tags.add(JsonArray(listOf(JsonPrimitive("value_minimum"), JsonPrimitive(minZapAmountInSats.toString()))))
        }
        if (maxZapAmountInSats != null) {
            tags.add(JsonArray(listOf(JsonPrimitive("value_maximum"), JsonPrimitive(maxZapAmountInSats.toString()))))
        }

        writeRelayUrls.forEach { relayUrl ->
            tags.add(JsonArray(listOf(JsonPrimitive("relay"), JsonPrimitive(relayUrl))))
        }

        return tags
    }

    private fun resolveRootEventTag(
        highlight: Nevent?,
        article: Naddr?,
        note: Nevent?,
    ) = when {
        highlight != null -> highlight.asEventTag(marker = "root")
        article != null -> article.asReplaceableEventTag(marker = "root")
        note != null -> note.asEventTag(marker = "root")
        else -> null
    }

    private fun resolveReplyEventTag(
        rootNote: Nevent?,
        rootHighlight: Nevent?,
        rootArticle: Naddr?,
        replyTo: Nevent?,
    ): JsonArray? {
        return when {
            rootArticle != null || rootHighlight != null -> {
                replyTo?.asEventTag(marker = "reply")
            }

            replyTo?.eventId != rootNote?.eventId -> {
                replyTo?.asEventTag(marker = "reply")
            }

            else -> null
        }
    }

    private suspend fun resolveReferencedPubkeyTags(
        rootNoteNevent: Nevent?,
        rootArticleNaddr: Naddr?,
        rootHighlightNevent: Nevent?,
        replyToNoteNevent: Nevent?,
    ): Set<JsonArray> {
        val replyToPostData = replyToNoteNevent?.let {
            feedRepository.findPostsById(postId = it.eventId)
        }

        val replyToExistingPubkeyTags = replyToPostData?.tags?.filter { it.isPubKeyTag() }?.toSet() ?: emptySet()
        val replyToAuthorPubkeyTag = replyToNoteNevent?.asPubkeyTag()
        val rootNoteAuthorPubkeyTag = rootNoteNevent?.asPubkeyTag()
        val rootHighlightAuthorPubkeyTag = rootHighlightNevent?.asPubkeyTag()
        val rootArticleAuthorPubkeyTag = rootArticleNaddr?.asPubkeyTag()

        return sequenceOf(
            replyToExistingPubkeyTags,
            listOfNotNull(
                replyToAuthorPubkeyTag,
                rootNoteAuthorPubkeyTag,
                rootHighlightAuthorPubkeyTag,
                rootArticleAuthorPubkeyTag,
            ),
        ).flatten().toSet()
    }

    private fun String.appendAttachmentUrls(attachments: List<NoteAttachment>): String {
        val attachmentUrls = attachments.mapNotNull { it.remoteUrl }
        val refinedContent = if (attachmentUrls.isEmpty()) {
            this
        } else {
            StringBuilder().apply {
                append(this@appendAttachmentUrls)
                appendLine()
                appendLine()
                attachmentUrls.forEachIndexed { index, url ->
                    append(url)
                    if (index < attachmentUrls.size - 1) appendLine()
                }
            }.toString()
        }
        return refinedContent
    }

    private fun Set<JsonArray>.insertRelayHints(relayHintsMap: Map<String, String>) =
        this.map {
            val noteId = it[1].jsonPrimitive.content
            val relayHint = relayHintsMap.getOrDefault(key = noteId, defaultValue = "")
            JsonArray(
                it.toMutableList().apply {
                    if (relayHint.isNotEmpty()) {
                        if (this.size > 2) {
                            this[2] = JsonPrimitive(relayHint)
                        } else {
                            add(index = 2, JsonPrimitive(relayHint))
                        }
                    }
                },
            )
        }

    private fun String.ensureWhitespaceBeforeUserTag(): String = this.replace(Regex("([^-\\s])(nostr:npub)"), "$1 $2")

    private data class PreparedNote(
        val refinedContent: String,
        val referenceTags: List<JsonArray>,
        val hashtagTags: Set<JsonArray>,
        val iMetaTags: List<JsonArray>,
        val outboxRelays: List<String>,
    )
}
