package net.primal.android.nostr.ext

import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.crypto.bechToBytes
import net.primal.android.crypto.toHex
import net.primal.android.feed.db.NostrResource
import net.primal.android.feed.db.PostData
import net.primal.android.feed.db.ReferencedPost
import net.primal.android.feed.db.ReferencedUser
import net.primal.android.messages.db.DirectMessageData
import net.primal.android.nostr.utils.Nip19TLV
import net.primal.android.profile.db.ProfileData
import timber.log.Timber
import java.util.regex.Pattern

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
    Pattern.CASE_INSENSITIVE
)

fun String.isNostrUri(): Boolean {
    val uri = lowercase()
    return uri.startsWith(NOSTR) || uri.startsWith(NPUB) || uri.startsWith(NOTE)
            || uri.startsWith(NEVENT) || uri.startsWith(NPROFILE)
}

fun String.isNote() = lowercase().startsWith(NOTE)

fun String.isNPub() = lowercase().startsWith(NPUB)

fun String.isNProfile() = lowercase().startsWith(NPROFILE)

fun String.isNoteUri() = lowercase().startsWith(NOSTR + NOTE)

fun String.isNEventUri() = lowercase().startsWith(NOSTR + NEVENT)

fun String.isNPubUri() = lowercase().startsWith(NOSTR + NPUB)

fun String.isNProfileUri() = lowercase().startsWith(NOSTR + NPROFILE)

fun String.parseNostrUris(): List<String> {
    return nostrUriRegexPattern.toRegex().findAll(this).map { matchResult ->
        matchResult.groupValues[1] + matchResult.groupValues[2] + matchResult.groupValues[3]
    }.toList()
}

private fun String.nostrUriToBytes(): ByteArray? {
    val matcher = nostrUriRegexPattern.matcher(this)
    if (!matcher.find()) return null
    val type = matcher.group(2)?.lowercase() ?: return null
    val key = matcher.group(3)?.lowercase() ?: return null
    return (type + key).bechToBytes()
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

fun String.extractProfileId() : String? {
    val matcher = nostrUriRegexPattern.matcher(this)
    if (!matcher.find()) return null

    val bechPrefix = matcher.group(2)
    val key = matcher.group(3)

    return try {
        when (bechPrefix?.lowercase()) {
            NPUB -> (bechPrefix + key).bechToBytes().toHex()
            NPROFILE -> {
                val tlv = Nip19TLV.parse((bechPrefix + key).bechToBytes())
                tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
            }
            else -> null
        }
    } catch (error: IllegalArgumentException) {
        Timber.e(error)
        null
    }
}

fun String.extractNoteId() : String? {
    val matcher = nostrUriRegexPattern.matcher(this)
    if (!matcher.find()) return null

    val bechPrefix = matcher.group(2)
    val key = matcher.group(3)

    return try {
        when (bechPrefix?.lowercase()) {
            NOTE -> (bechPrefix + key).bechToBytes().toHex()
            NEVENT -> {
                val tlv = Nip19TLV.parse((bechPrefix + key).bechToBytes())
                tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
            }
            else -> null
        }
    } catch (error: IllegalArgumentException) {
        Timber.e(error)
        null
    }
}

fun List<PostData>.flatMapPostsAsNostrResourcePO(
    postIdToPostDataMap: Map<String, PostData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
): List<NostrResource> = flatMap { postData ->
    postData.uris.mapAsNostrResourcePO(
        eventId = postData.postId,
        postIdToPostDataMap = postIdToPostDataMap,
        profileIdToProfileDataMap = profileIdToProfileDataMap,
    )
}

fun List<DirectMessageData>.flatMapMessagesAsNostrResourcePO(
    postIdToPostDataMap: Map<String, PostData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
) = flatMap { messageData ->
    messageData.uris.mapAsNostrResourcePO(
        eventId = messageData.messageId,
        postIdToPostDataMap = postIdToPostDataMap,
        profileIdToProfileDataMap = profileIdToProfileDataMap,
    )
}

fun List<String>.mapAsNostrResourcePO(
    eventId: String,
    postIdToPostDataMap: Map<String, PostData>,
    profileIdToProfileDataMap: Map<String, ProfileData>,
) = filter { it.isNostrUri() }.map { link ->
    val refPostId = link.extractNoteId()
    val refUserProfileId = link.extractProfileId()
    val refPost = postIdToPostDataMap[refPostId]
    val refPostAuthor = profileIdToProfileDataMap[refPost?.authorId]

    NostrResource(
        postId = eventId,
        uri = link,
        referencedUser = if (refUserProfileId != null) ReferencedUser(
            userId = refUserProfileId,
            handle = profileIdToProfileDataMap[refUserProfileId]
                ?.usernameUiFriendly()
                ?: refUserProfileId.asEllipsizedNpub(),
        ) else null,
        referencedPost = if (refPost != null && refPostAuthor != null) ReferencedPost(
            postId = refPost.postId,
            createdAt = refPost.createdAt,
            content = refPost.content,
            authorId = refPost.authorId,
            authorName = refPostAuthor.authorNameUiFriendly(),
            authorAvatarUrl = refPostAuthor.picture,
            authorInternetIdentifier = refPostAuthor.internetIdentifier,
            authorLightningAddress = refPostAuthor.lightningAddress,
            mediaResources = listOf(refPost).flatMapPostsAsMediaResourcePO(),
            nostrResources = listOf(refPost).flatMapPostsAsNostrResourcePO(
                postIdToPostDataMap = postIdToPostDataMap,
                profileIdToProfileDataMap = profileIdToProfileDataMap,
            ),
        ) else null,
    )
}
