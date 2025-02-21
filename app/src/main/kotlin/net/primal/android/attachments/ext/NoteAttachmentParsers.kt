package net.primal.android.attachments.ext

import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.domain.CdnResource
import net.primal.android.attachments.domain.LinkPreviewData
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.core.utils.detectMimeType
import net.primal.android.messages.db.DirectMessageData
import net.primal.android.nostr.ext.isNostrUri
import net.primal.android.notes.db.PostData

fun List<PostData>.flatMapPostsAsNoteAttachmentPO(
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, LinkPreviewData>,
    videoThumbnails: Map<String, String>,
) = flatMap { postData ->
    postData.uris.map { uri ->
        postData.postId to uri
    }
}
    .filterNot { (_, uri) -> uri.isNostrUri() }
    .map { (eventId, uri) ->
        val uriCdnResource = cdnResources[uri]
        val linkPreview = linkPreviews[uri]
        val linkThumbnailCdnResource = linkPreview?.thumbnailUrl?.let { cdnResources[it] }
        val videoThumbnail = videoThumbnails[uri]
        val mimeType = uri.detectMimeType() ?: uriCdnResource?.contentType ?: linkPreview?.mimeType
        val type = detectNoteAttachmentType(url = uri, mimeType = mimeType)
        NoteAttachment(
            eventId = eventId,
            url = uri,
            type = type,
            mimeType = mimeType,
            variants = (uriCdnResource?.variants ?: emptyList()) + (linkThumbnailCdnResource?.variants ?: emptyList()),
            title = linkPreview?.title?.ifBlank { null },
            description = linkPreview?.description?.ifBlank { null },
            thumbnail = linkPreview?.thumbnailUrl?.ifBlank { null } ?: videoThumbnail,
            authorAvatarUrl = linkPreview?.authorAvatarUrl?.ifBlank { null },
        )
    }

fun List<DirectMessageData>.flatMapMessagesAsNoteAttachmentPO() =
    flatMap { messageData ->
        messageData.uris.map { uri ->
            messageData.messageId to uri
        }
    }
        .filterNot { (_, uri) -> uri.isNostrUri() }
        .map { (messageId, uri) ->
            val mimeType = uri.detectMimeType()
            NoteAttachment(
                eventId = messageId,
                url = uri,
                type = detectNoteAttachmentType(url = uri, mimeType = mimeType),
                mimeType = mimeType,
            )
        }

private fun detectNoteAttachmentType(url: String, mimeType: String?): NoteAttachmentType {
    mimeType?.let {
        val mimeTypeAttachment = detectMimeTypeAttachment(mimeType)
        if (mimeTypeAttachment != NoteAttachmentType.Other) {
            return mimeTypeAttachment
        }
    }

    return detectUrlAttachmentType(url)
}

private fun detectMimeTypeAttachment(mimeType: String): NoteAttachmentType {
    return when {
        mimeType.startsWith("image") -> NoteAttachmentType.Image
        mimeType.startsWith("video") -> NoteAttachmentType.Video
        mimeType.startsWith("audio") -> NoteAttachmentType.Audio
        mimeType.endsWith("pdf") -> NoteAttachmentType.Pdf
        else -> NoteAttachmentType.Other
    }
}

private fun detectUrlAttachmentType(url: String): NoteAttachmentType {
    return when {
        url.contains(".youtube.com") -> NoteAttachmentType.YouTube
        url.contains("/youtube.com") -> NoteAttachmentType.YouTube
        url.contains("/youtu.be") -> NoteAttachmentType.YouTube
        url.contains(".rumble.com") || url.contains("/rumble.com") -> NoteAttachmentType.Rumble
        url.contains("/open.spotify.com/") -> NoteAttachmentType.Spotify
        url.contains("/listen.tidal.com/") -> NoteAttachmentType.Tidal
        url.contains("/github.com/") -> NoteAttachmentType.GitHub
        else -> NoteAttachmentType.Other
    }
}
