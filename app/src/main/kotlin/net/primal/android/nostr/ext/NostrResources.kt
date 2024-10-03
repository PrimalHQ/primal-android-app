package net.primal.android.nostr.ext

import java.util.regex.Pattern
import net.primal.android.articles.db.ArticleData
import net.primal.android.articles.feed.ui.wordsCountToReadingTime
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.attachments.ext.flatMapPostsAsNoteAttachmentPO
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.crypto.bech32ToHexOrThrow
import net.primal.android.crypto.bechToBytesOrThrow
import net.primal.android.crypto.toHex
import net.primal.android.messages.db.DirectMessageData
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.utils.Nip19TLV
import net.primal.android.nostr.utils.Nip19TLV.toNaddrString
import net.primal.android.notes.db.PostData
import net.primal.android.notes.db.ReferencedArticle
import net.primal.android.notes.db.ReferencedNote
import net.primal.android.notes.db.ReferencedUser
import net.primal.android.profile.db.ProfileData
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

private fun String.nostrUriToBytes(): ByteArray? {
    val matcher = nostrUriRegexPattern.matcher(this)
    if (!matcher.find()) return null
    val type = matcher.group(2)?.lowercase() ?: return null
    val key = matcher.group(3)?.lowercase() ?: return null
    return try {
        (type + key).bechToBytesOrThrow()
    } catch (error: Exception) {
        Timber.w(error)
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

private fun String.extract(parser: (bechPrefix: String?, key: String?) -> String?): String? {
    val matcher = nostrUriRegexPattern.matcher(this)
    if (!matcher.find()) return null

    val bechPrefix = matcher.group(2)
    val key = matcher.group(3)

    return try {
        parser(bechPrefix, key)
    } catch (error: Exception) {
        Timber.w(error)
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
            Nip19TLV.parseAsNaddr(this)
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
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
): List<NoteNostrUri> =
    flatMap { postData ->
        postData.uris.mapAsNoteNostrUriPO(
            eventId = postData.postId,
            postIdToPostDataMap = postIdToPostDataMap,
            articleIdToArticle = articleIdToArticle,
            profileIdToProfileDataMap = profileIdToProfileDataMap,
        )
    }

fun List<DirectMessageData>.flatMapMessagesAsNostrResourcePO(
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
) = flatMap { messageData ->
    messageData.uris.mapAsNoteNostrUriPO(
        eventId = messageData.messageId,
        postIdToPostDataMap = postIdToPostDataMap,
        articleIdToArticle = articleIdToArticle,
        profileIdToProfileDataMap = profileIdToProfileDataMap,
    )
}

fun List<String>.mapAsNoteNostrUriPO(
    eventId: String,
    postIdToPostDataMap: Map<String, PostData>,
    articleIdToArticle: Map<String, ArticleData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
) = filter { it.isNostrUri() }.map { link ->
    val refUserProfileId = link.extractProfileId()

    val refNoteId = link.extractNoteId()
    val refNote = postIdToPostDataMap[refNoteId]
    val refPostAuthor = profileIdToProfileDataMap[refNote?.authorId]

    val refNaddr = Nip19TLV.parseAsNaddr(link.removePrefix("nostr:"))
    val refArticle = articleIdToArticle[refNaddr?.identifier]
    val refArticleAuthor = profileIdToProfileDataMap[refNaddr?.userId]

    NoteNostrUri(
        noteId = eventId,
        uri = link,
        referencedUser = if (refUserProfileId != null) {
            ReferencedUser(
                userId = refUserProfileId,
                handle = profileIdToProfileDataMap[refUserProfileId]?.usernameUiFriendly()
                    ?: refUserProfileId.asEllipsizedNpub(),
            )
        } else {
            null
        },
        referencedNote = if (refNote != null && refPostAuthor != null) {
            ReferencedNote(
                postId = refNote.postId,
                createdAt = refNote.createdAt,
                content = refNote.content,
                authorId = refNote.authorId,
                authorName = refPostAuthor.authorNameUiFriendly(),
                authorAvatarCdnImage = refPostAuthor.avatarCdnImage,
                authorInternetIdentifier = refPostAuthor.internetIdentifier,
                authorLightningAddress = refPostAuthor.lightningAddress,
                attachments = listOf(refNote).flatMapPostsAsNoteAttachmentPO(
                    cdnResources = emptyMap(),
                    linkPreviews = emptyMap(),
                    videoThumbnails = emptyMap(),
                ),
                nostrUris = listOf(refNote).flatMapPostsAsNoteNostrUriPO(
                    postIdToPostDataMap = postIdToPostDataMap,
                    articleIdToArticle = articleIdToArticle,
                    profileIdToProfileDataMap = profileIdToProfileDataMap,
                ),
            )
        } else {
            null
        },
        referencedArticle = if (
            refNaddr?.kind == NostrEventKind.LongFormContent.value &&
            refArticle != null &&
            refArticleAuthor != null
        ) {
            ReferencedArticle(
                naddr = refNaddr.toNaddrString(),
                eventId = refArticle.eventId,
                articleId = refArticle.articleId,
                articleTitle = refArticle.title,
                authorId = refArticle.authorId,
                authorName = refArticleAuthor.authorNameUiFriendly(),
                authorAvatarCdnImage = refArticleAuthor.avatarCdnImage,
                createdAt = refArticle.createdAt,
                raw = refArticle.raw,
                articleImageCdnImage = refArticle.imageCdnImage,
                articleReadingTimeInMinutes = refArticle.wordsCount.wordsCountToReadingTime(),
            )
        } else {
            null
        },
    )
}
