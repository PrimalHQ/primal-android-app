package net.primal.data.repository.mappers.remote

import kotlinx.serialization.json.JsonArray
import net.primal.core.utils.toDouble
import net.primal.data.local.dao.events.EventUriNostr
import net.primal.data.local.dao.messages.DirectMessageData
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.dao.reads.ArticleData
import net.primal.data.repository.mappers.authorNameUiFriendly
import net.primal.data.repository.mappers.usernameUiFriendly
import net.primal.domain.links.CdnResource
import net.primal.domain.links.EventLinkPreviewData
import net.primal.domain.links.EventUriNostrReference
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.ReferencedArticle
import net.primal.domain.links.ReferencedHighlight
import net.primal.domain.links.ReferencedNote
import net.primal.domain.links.ReferencedUser
import net.primal.domain.links.ReferencedZap
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.findFirstAltDescription
import net.primal.domain.nostr.findFirstBolt11
import net.primal.domain.nostr.findFirstEventId
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.findFirstZapAmount
import net.primal.domain.nostr.isATag
import net.primal.domain.nostr.utils.LnInvoiceUtils
import net.primal.domain.nostr.utils.asEllipsizedNpub
import net.primal.domain.nostr.utils.extractEventId
import net.primal.domain.nostr.utils.extractNoteId
import net.primal.domain.nostr.utils.extractProfileId
import net.primal.domain.nostr.utils.isNostrUri
import net.primal.domain.reads.wordsCountToReadingTime

fun PostData.toNevent() =
    Nevent(
        userId = this.authorId,
        eventId = this.postId,
        kind = NostrEventKind.ShortTextNote.value,
        relays = emptyList(),
    )

fun List<EventUriNostrReference>.mapReferencedNostrUriAsEventUriNostrPO() =
    map {
        EventUriNostr(
            eventId = it.eventId,
            uri = it.uri,
            type = it.type,
            referencedEventAlt = it.referencedEventAlt,
            referencedHighlight = it.referencedHighlight,
            referencedNote = it.referencedNote,
            referencedArticle = it.referencedArticle,
            referencedUser = it.referencedUser,
            referencedZap = it.referencedZap,
        )
    }

fun List<PostData>.flatMapPostsAsReferencedNostrUriDO(
    eventIdToNostrEvent: Map<String, NostrEvent>,
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
): List<EventUriNostrReference> =
    flatMap { postData ->
        postData.uris.mapAsReferencedNostrUriDO(
            eventId = postData.postId,
            eventIdToNostrEvent = eventIdToNostrEvent,
            postIdToPostDataMap = postIdToPostDataMap,
            articleIdToArticle = articleIdToArticle,
            profileIdToProfileDataMap = profileIdToProfileDataMap,
            cdnResources = cdnResources,
            linkPreviews = linkPreviews,
            videoThumbnails = videoThumbnails,
        )
    }

fun List<DirectMessageData>.flatMapMessagesAsReferencedNostrUriDO(
    eventIdToNostrEvent: Map<String, NostrEvent>,
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
) = flatMap { messageData ->
    messageData.uris.mapAsReferencedNostrUriDO(
        eventId = messageData.messageId,
        eventIdToNostrEvent = eventIdToNostrEvent,
        postIdToPostDataMap = postIdToPostDataMap,
        articleIdToArticle = articleIdToArticle,
        profileIdToProfileDataMap = profileIdToProfileDataMap,
        cdnResources = cdnResources,
        linkPreviews = linkPreviews,
        videoThumbnails = videoThumbnails,
    )
}

fun List<String>.mapAsReferencedNostrUriDO(
    eventId: String,
    eventIdToNostrEvent: Map<String, NostrEvent>,
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
) = filter { it.isNostrUri() }.map { link ->
    val refUserProfileId = link.extractProfileId()

    val refNoteId = link.extractNoteId()
    val refNote = postIdToPostDataMap[refNoteId]
    val refPostAuthor = profileIdToProfileDataMap[refNote?.authorId]

    val refNaddr: Naddr? = Nip19TLV.parseUriAsNaddrOrNull(link)
    val refArticle = articleIdToArticle[refNaddr?.identifier]
    val refArticleAuthor = profileIdToProfileDataMap[refNaddr?.userId]

    val referencedNostrEvent: NostrEvent? = eventIdToNostrEvent[link.extractEventId()]

    val refHighlightText = referencedNostrEvent?.content
    val refHighlightATag = referencedNostrEvent?.tags?.firstOrNull { it.isATag() }

    val type = when {
        refUserProfileId != null -> EventUriNostrType.Profile
        refNote != null && refPostAuthor != null -> EventUriNostrType.Note
        refNaddr?.kind == NostrEventKind.LongFormContent.value &&
            refArticle != null && refArticleAuthor != null -> EventUriNostrType.Article

        referencedNostrEvent?.kind == NostrEventKind.Highlight.value &&
            refHighlightText?.isNotEmpty() == true && refHighlightATag != null -> EventUriNostrType.Highlight

        referencedNostrEvent?.kind == NostrEventKind.Zap.value -> EventUriNostrType.Zap

        else -> EventUriNostrType.Unsupported
    }

    EventUriNostrReference(
        eventId = eventId,
        uri = link,
        type = type,
        referencedEventAlt = referencedNostrEvent?.tags?.findFirstAltDescription(),
        referencedUser = takeAsReferencedUserOrNull(refUserProfileId, profileIdToProfileDataMap),
        referencedNote = takeAsReferencedNoteOrNull(
            refNote = refNote,
            refPostAuthor = refPostAuthor,
            cdnResources = cdnResources,
            linkPreviews = linkPreviews,
            videoThumbnails = videoThumbnails,
            eventIdToNostrEvent = eventIdToNostrEvent,
            postIdToPostDataMap = postIdToPostDataMap,
            articleIdToArticle = articleIdToArticle,
            profileIdToProfileDataMap = profileIdToProfileDataMap,
        ),
        referencedArticle = takeAsReferencedArticleOrNull(refNaddr, refArticle, refArticleAuthor),
        referencedZap = takeAsReferencedZapOrNull(
            event = referencedNostrEvent,
            profilesMap = profileIdToProfileDataMap,
            postsMap = postIdToPostDataMap,
            cdnResourcesMap = cdnResources,
            linkPreviewsMap = linkPreviews,
            nostrEventsMap = eventIdToNostrEvent,
            videoThumbnailsMap = videoThumbnails,
            articlesMap = articleIdToArticle,
        ),
        referencedHighlight = takeAsReferencedHighlightOrNull(
            uri = link,
            highlight = refHighlightText,
            aTag = refHighlightATag,
            authorId = referencedNostrEvent?.tags?.findFirstProfileId(),
        ),
    )
}

private fun takeAsReferencedNoteOrNull(
    refNote: PostData?,
    refPostAuthor: ProfileData?,
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
    eventIdToNostrEvent: Map<String, NostrEvent>,
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
) = if (refNote != null && refPostAuthor != null) {
    ReferencedNote(
        postId = refNote.postId,
        createdAt = refNote.createdAt,
        content = refNote.content,
        authorId = refNote.authorId,
        authorName = refPostAuthor.authorNameUiFriendly(),
        authorAvatarCdnImage = refPostAuthor.avatarCdnImage,
        authorInternetIdentifier = refPostAuthor.internetIdentifier,
        authorLightningAddress = refPostAuthor.lightningAddress,
        authorLegendProfile = refPostAuthor.primalPremiumInfo?.legendProfile,
        attachments = listOf(refNote).flatMapPostsAsEventUriPO(
            cdnResources = cdnResources,
            linkPreviews = linkPreviews,
            videoThumbnails = videoThumbnails,
        ).mapEventUriAsNoteLinkDO(),
        nostrUris = listOf(refNote).flatMapPostsAsReferencedNostrUriDO(
            eventIdToNostrEvent = eventIdToNostrEvent,
            postIdToPostDataMap = postIdToPostDataMap,
            articleIdToArticle = articleIdToArticle,
            profileIdToProfileDataMap = profileIdToProfileDataMap,
            cdnResources = cdnResources,
            linkPreviews = linkPreviews,
            videoThumbnails = videoThumbnails,
        ),
        raw = refNote.raw,
    )
} else {
    null
}

private fun takeAsReferencedUserOrNull(
    refUserProfileId: String?,
    profileIdToProfileDataMap: Map<String, ProfileData>,
) = if (refUserProfileId != null) {
    ReferencedUser(
        userId = refUserProfileId,
        handle = profileIdToProfileDataMap[refUserProfileId]?.usernameUiFriendly()
            ?: refUserProfileId.asEllipsizedNpub(),
    )
} else {
    null
}

private fun takeAsReferencedArticleOrNull(
    refNaddr: Naddr?,
    refArticle: ArticleData?,
    refArticleAuthor: ProfileData?,
) = if (
    refNaddr?.kind == NostrEventKind.LongFormContent.value &&
    refArticle != null &&
    refArticleAuthor != null
) {
    ReferencedArticle(
        naddr = refNaddr.toNaddrString(),
        aTag = refArticle.aTag,
        eventId = refArticle.eventId,
        articleId = refArticle.articleId,
        articleTitle = refArticle.title,
        authorId = refArticle.authorId,
        authorName = refArticleAuthor.authorNameUiFriendly(),
        authorAvatarCdnImage = refArticleAuthor.avatarCdnImage,
        authorLegendProfile = refArticleAuthor.primalPremiumInfo?.legendProfile,
        createdAt = refArticle.createdAt,
        raw = refArticle.raw,
        articleImageCdnImage = refArticle.imageCdnImage,
        articleReadingTimeInMinutes = refArticle.wordsCount.wordsCountToReadingTime(),
    )
} else {
    null
}

private fun takeAsReferencedHighlightOrNull(
    uri: String,
    highlight: String?,
    aTag: JsonArray?,
    authorId: String?,
) = if (highlight?.isNotEmpty() == true && aTag != null) {
    val nevent = Nip19TLV.parseUriAsNeventOrNull(neventUri = uri)
    ReferencedHighlight(
        text = highlight,
        aTag = aTag,
        eventId = nevent?.eventId,
        authorId = authorId,
    )
} else {
    null
}

private fun takeAsReferencedZapOrNull(
    event: NostrEvent?,
    profilesMap: Map<String, ProfileData>,
    postsMap: Map<String, PostData>,
    cdnResourcesMap: Map<String, CdnResource>,
    linkPreviewsMap: Map<String, EventLinkPreviewData>,
    nostrEventsMap: Map<String, NostrEvent>,
    videoThumbnailsMap: Map<String, String>,
    articlesMap: Map<String, ArticleData>,
): ReferencedZap? {
    val zapRequest = event?.extractZapRequestOrNull()

    val receiverId = event?.tags?.findFirstProfileId()

    val senderId = zapRequest?.pubKey

    val noteId = event?.tags?.findFirstEventId()
        ?: zapRequest?.tags?.findFirstEventId()

    val amountInSats = (event?.tags?.findFirstBolt11() ?: zapRequest?.tags?.findFirstZapAmount())
        ?.let(LnInvoiceUtils::getAmountInSats)

    if (receiverId == null || senderId == null || amountInSats == null) return null

    val zappedPost = postsMap[noteId]

    val nostrUris = listOfNotNull(zappedPost).flatMapPostsAsReferencedNostrUriDO(
        eventIdToNostrEvent = nostrEventsMap,
        postIdToPostDataMap = postsMap,
        articleIdToArticle = articlesMap,
        profileIdToProfileDataMap = profilesMap,
        cdnResources = cdnResourcesMap,
        videoThumbnails = videoThumbnailsMap,
        linkPreviews = linkPreviewsMap,
    )

    val sender = profilesMap[senderId]
    val receiver = profilesMap[receiverId]
    return ReferencedZap(
        senderId = senderId,
        senderAvatarCdnImage = sender?.avatarCdnImage,
        senderPrimalLegendProfile = sender?.primalPremiumInfo?.legendProfile,
        receiverId = receiverId,
        receiverDisplayName = receiver?.displayName ?: receiver?.handle,
        receiverAvatarCdnImage = receiver?.avatarCdnImage,
        receiverPrimalLegendProfile = receiver?.primalPremiumInfo?.legendProfile,
        amountInSats = amountInSats.toDouble(),
        message = zapRequest.content,
        zappedEventId = noteId,
        zappedEventContent = zappedPost?.content,
        zappedEventNostrUris = nostrUris,
        zappedEventHashtags = zappedPost?.hashtags ?: emptyList(),
        createdAt = event.createdAt,
    )
}
