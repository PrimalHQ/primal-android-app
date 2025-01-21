package net.primal.android.nostr.ext

import java.util.regex.Pattern
import kotlinx.serialization.json.JsonArray
import net.primal.android.articles.db.ArticleData
import net.primal.android.articles.feed.ui.wordsCountToReadingTime
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.attachments.domain.CdnResource
import net.primal.android.attachments.domain.LinkPreviewData
import net.primal.android.attachments.domain.NostrUriType
import net.primal.android.attachments.ext.flatMapPostsAsNoteAttachmentPO
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.crypto.bech32ToHexOrThrow
import net.primal.android.crypto.bechToBytesOrThrow
import net.primal.android.crypto.toHex
import net.primal.android.messages.db.DirectMessageData
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.utils.Naddr
import net.primal.android.nostr.utils.Nevent
import net.primal.android.nostr.utils.Nip19TLV
import net.primal.android.nostr.utils.Nip19TLV.toNaddrString
import net.primal.android.notes.db.PostData
import net.primal.android.notes.db.ReferencedArticle
import net.primal.android.notes.db.ReferencedHighlight
import net.primal.android.notes.db.ReferencedNote
import net.primal.android.notes.db.ReferencedUser
import net.primal.android.notes.db.ReferencedZap
import net.primal.android.profile.db.ProfileData
import net.primal.android.wallet.utils.LnInvoiceUtils
import timber.log.Timber

private const val NOSTR = "nostr:"
private const val NPUB = "npub1"
private const val NSEC = "nsec1"
private const val NEVENT = "nevent1"
private const val NADDR = "naddr1"
private const val NOTE = "note1"
private const val NRELAY = "nrelay1"
private const val NPROFILE = "nprofile1"

private val nostrUriRegexPattern: Pattern = Pattern.compile(
    "($NOSTR)?@?($NSEC|$NPUB|$NEVENT|$NADDR|$NOTE|$NPROFILE|$NRELAY)([qpzry9x8gf2tvdw0s3jn54khce6mua7l]+)([\\S]*)",
    Pattern.CASE_INSENSITIVE,
)

private val urlRegexPattern: Pattern = Pattern.compile(
    "https?://(www\\.)?[-a-zA-Z0-9@:%.+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()_@:%+.~#?&//=]*)",
    Pattern.CASE_INSENSITIVE,
)

fun String.isNostrUri(): Boolean {
    val uri = lowercase()
    return uri.startsWith(NOSTR) || uri.startsWith(NPUB) || uri.startsWith(NOTE) ||
        uri.startsWith(NEVENT) || uri.startsWith(NPROFILE)
}

fun String.cleanNostrUris(): String =
    this
        .replace("@$NOSTR", NOSTR)
        .replace("@$NPUB", NPUB)
        .replace("@$NOTE", NOTE)
        .replace("@$NEVENT", NEVENT)
        .replace("@$NADDR", NADDR)
        .replace("@$NPROFILE", NPROFILE)

fun String.isNote() = lowercase().startsWith(NOTE)

fun String.isNPub() = lowercase().startsWith(NPUB)

fun String.isNProfile() = lowercase().startsWith(NPROFILE)

fun String.isNAddr() = lowercase().startsWith(NADDR)

fun String.isNEvent() = lowercase().startsWith(NEVENT)

fun String.isNoteUri() = lowercase().startsWith(NOSTR + NOTE)

fun String.isNEventUri() = lowercase().startsWith(NOSTR + NEVENT)

fun String.isNPubUri() = lowercase().startsWith(NOSTR + NPUB)

fun String.isNProfileUri() = lowercase().startsWith(NOSTR + NPROFILE)

fun String.isNAddrUri() = lowercase().startsWith(NOSTR + NADDR)

fun String.parseNostrUris(): List<String> {
    return nostrUriRegexPattern.toRegex().findAll(this).map { matchResult ->
        matchResult.groupValues[1] + matchResult.groupValues[2] + matchResult.groupValues[3]
    }.filter { it.nostrUriToBytes() != null }.toList()
}

fun String.detectUrls(): List<String> {
    val urlRegex = urlRegexPattern.toRegex()
    return urlRegex.findAll(this).map { matchResult ->
        val url = matchResult.groupValues[0]
        val startIndex = matchResult.range.first
        val charBefore = this.getOrNull(startIndex - 1)

        when (charBefore) {
            '(' -> url.trimEnd(')')
            '[' -> url.trimEnd(']')
            else -> url
        }
    }.toList()
}

private fun String.nostrUriToBytes(): ByteArray? {
    val matcher = nostrUriRegexPattern.matcher(this)
    if (!matcher.find()) return null
    val type = matcher.group(2)?.lowercase() ?: return null
    val key = matcher.group(3)?.lowercase() ?: return null
    return try {
        (type + key).bechToBytesOrThrow()
    } catch (ignored: Exception) {
        Timber.w(ignored)
        null
    }
}

fun String.nostrUriToNoteId() = nostrUriToBytes()?.toHex()

fun String.nostrUriToPubkey() = nostrUriToBytes()?.toHex()

private fun String.nostrUriToIdAndRelay(): Pair<String?, String?> {
    val bytes = nostrUriToBytes() ?: return null to null
    val tlv = Nip19TLV.parse(bytes)
    val id = tlv[Nip19TLV.Type.SPECIAL.id]?.firstOrNull()?.toHex()
    val relayBytes = tlv[Nip19TLV.Type.RELAY.id]?.firstOrNull()
    return id to relayBytes?.let { String(it) }
}

fun String.nostrUriToNoteIdAndRelay() = nostrUriToIdAndRelay()

fun String.nostrUriToPubkeyAndRelay() = nostrUriToIdAndRelay()

fun String.extractProfileId(): String? {
    return extract { bechPrefix: String?, key: String? ->
        when (bechPrefix?.lowercase()) {
            NPUB -> (bechPrefix + key).bechToBytesOrThrow().toHex()
            NPROFILE -> {
                val tlv = Nip19TLV.parse((bechPrefix + key).bechToBytesOrThrow())
                tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
            }

            else -> null
        }
    }
}

fun String.extractNoteId(): String? {
    return extract { bechPrefix: String?, key: String? ->
        when (bechPrefix?.lowercase()) {
            NOTE -> (bechPrefix + key).bechToBytesOrThrow().toHex()
            NEVENT -> {
                val tlv = Nip19TLV.parse((bechPrefix + key).bechToBytesOrThrow())
                tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
            }

            else -> null
        }
    }
}

fun String.extractEventId(): String? {
    return extract { bechPrefix: String?, key: String? ->
        when (bechPrefix?.lowercase()) {
            NEVENT -> {
                val tlv = Nip19TLV.parse((bechPrefix + key).bechToBytesOrThrow())
                tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
            }

            else -> (bechPrefix + key).bechToBytesOrThrow().toHex()
        }
    }
}

fun PostData.toNevent() =
    Nevent(
        userId = this.authorId,
        eventId = this.postId,
        kind = NostrEventKind.ShortTextNote.value,
        relays = emptyList(),
    )

private fun String.extract(parser: (bechPrefix: String?, key: String?) -> String?): String? {
    val matcher = nostrUriRegexPattern.matcher(this)
    if (!matcher.find()) return null

    val bechPrefix = matcher.group(2)
    val key = matcher.group(3)

    return try {
        parser(bechPrefix, key)
    } catch (ignored: Exception) {
        Timber.w(ignored)
        null
    }
}

fun String.takeAsNoteHexIdOrNull(): String? {
    return if (isNote() || isNoteUri() || isNEventUri() || isNEvent()) {
        val result = runCatching { this.extractNoteId() }
        result.getOrNull()
    } else {
        null
    }
}

fun String.takeAsProfileHexIdOrNull(): String? {
    return if (isNPub() || isNPubUri()) {
        val result = runCatching {
            this.bech32ToHexOrThrow()
        }
        result.getOrNull()
    } else {
        null
    }
}

fun String.takeAsNaddrOrNull(): String? {
    return if (isNAddr() || isNAddrUri()) {
        val result = runCatching {
            Nip19TLV.parseUriAsNaddrOrNull(this)
        }
        if (result.getOrNull() != null) {
            this
        } else {
            null
        }
    } else {
        null
    }
}

fun List<PostData>.flatMapPostsAsNoteNostrUriPO(
    eventIdToNostrEvent: Map<String, NostrEvent>,
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, LinkPreviewData>,
    videoThumbnails: Map<String, String>,
): List<NoteNostrUri> =
    flatMap { postData ->
        postData.uris.mapAsNoteNostrUriPO(
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

fun List<DirectMessageData>.flatMapMessagesAsNostrResourcePO(
    eventIdToNostrEvent: Map<String, NostrEvent>,
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, LinkPreviewData>,
    videoThumbnails: Map<String, String>,
) = flatMap { messageData ->
    messageData.uris.mapAsNoteNostrUriPO(
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

fun List<String>.mapAsNoteNostrUriPO(
    eventId: String,
    eventIdToNostrEvent: Map<String, NostrEvent>,
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, LinkPreviewData>,
    videoThumbnails: Map<String, String>,
) = filter { it.isNostrUri() }.map { link ->
    val refUserProfileId = link.extractProfileId()

    val refNoteId = link.extractNoteId()
    val refNote = postIdToPostDataMap[refNoteId]
    val refPostAuthor = profileIdToProfileDataMap[refNote?.authorId]

    val refNaddr = Nip19TLV.parseUriAsNaddrOrNull(link)
    val refArticle = articleIdToArticle[refNaddr?.identifier]
    val refArticleAuthor = profileIdToProfileDataMap[refNaddr?.userId]

    val referencedNostrEvent: NostrEvent? = eventIdToNostrEvent[link.extractEventId()]

    val refHighlightText = referencedNostrEvent?.content
    val refHighlightATag = referencedNostrEvent?.tags?.firstOrNull { it.isATag() }

    val type = when {
        refUserProfileId != null -> NostrUriType.Profile
        refNote != null && refPostAuthor != null -> NostrUriType.Note
        refNaddr?.kind == NostrEventKind.LongFormContent.value &&
            refArticle != null && refArticleAuthor != null -> NostrUriType.Article

        referencedNostrEvent?.kind == NostrEventKind.Highlight.value &&
            refHighlightText?.isNotEmpty() == true && refHighlightATag != null -> NostrUriType.Highlight

        referencedNostrEvent?.kind == NostrEventKind.Zap.value -> NostrUriType.Zap

        else -> NostrUriType.Unsupported
    }

    NoteNostrUri(
        noteId = eventId,
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
        referencedZap = takeAsReferencedZapOrNull(referencedNostrEvent, profileIdToProfileDataMap),
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
    linkPreviews: Map<String, LinkPreviewData>,
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
        attachments = listOf(refNote).flatMapPostsAsNoteAttachmentPO(
            cdnResources = cdnResources,
            linkPreviews = linkPreviews,
            videoThumbnails = videoThumbnails,
        ),
        nostrUris = listOf(refNote).flatMapPostsAsNoteNostrUriPO(
            eventIdToNostrEvent = eventIdToNostrEvent,
            postIdToPostDataMap = postIdToPostDataMap,
            articleIdToArticle = articleIdToArticle,
            profileIdToProfileDataMap = profileIdToProfileDataMap,
            cdnResources = cdnResources,
            linkPreviews = linkPreviews,
            videoThumbnails = videoThumbnails,
        ),
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

private fun takeAsReferencedZapOrNull(event: NostrEvent?, profilesMap: Map<String, ProfileData>): ReferencedZap? {
    val zapRequest = event?.extractZapRequestOrNull()

    val receiverId = event?.tags?.findFirstProfileId()

    val senderId = zapRequest?.pubKey

    val noteId = event?.tags?.findFirstEventId()
        ?: zapRequest?.tags?.findFirstEventId()

    val amountInSats = (event?.tags?.findFirstBolt11() ?: zapRequest?.tags?.findFirstZapAmount())
        ?.let(LnInvoiceUtils::getAmountInSats)

    if (receiverId == null || senderId == null || amountInSats == null) return null

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
    )
}
