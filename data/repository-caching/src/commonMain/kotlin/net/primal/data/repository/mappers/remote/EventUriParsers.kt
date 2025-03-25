package net.primal.data.repository.mappers.remote

import net.primal.core.utils.detectMimeType
import net.primal.data.local.dao.events.EventUri
import net.primal.data.local.dao.messages.DirectMessageData
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.reads.ArticleData
import net.primal.domain.CdnResource
import net.primal.domain.EventLink
import net.primal.domain.EventLinkPreviewData
import net.primal.domain.EventUriType

private data class EventIdUriPair(
    val eventId: String,
    val uri: String,
)

fun List<PostData>.flatMapPostsAsEventUriPO(
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
): List<EventUri> =
    flatMap { postData ->
        postData.uris.map { uri -> EventIdUriPair(eventId = postData.postId, uri = uri) }
    }
        .filterNot { it.uri.isNostrUri() }
        .mapToEventUri(
            cdnResources = cdnResources,
            linkPreviews = linkPreviews,
            videoThumbnails = videoThumbnails,
        )

fun List<EventUri>.mapEventUriAsNoteLinkDO(): List<EventLink> =
    map { eventUri ->
        EventLink(
            eventId = eventUri.eventId,
            position = eventUri.position,
            url = eventUri.url,
            type = eventUri.type,
            mimeType = eventUri.mimeType,
            variants = eventUri.variants,
            title = eventUri.title,
            description = eventUri.description,
            thumbnail = eventUri.thumbnail,
            authorAvatarUrl = eventUri.authorAvatarUrl,
        )
    }

fun List<DirectMessageData>.flatMapMessagesAsEventUriPO() =
    flatMap { messageData ->
        messageData.uris.map { uri -> EventIdUriPair(eventId = messageData.messageId, uri = uri) }
    }
        .filterNot { it.uri.isNostrUri() }
        .map { (eventId, uri) ->
            val mimeType = uri.detectMimeType()
            EventUri(
                eventId = eventId,
                url = uri,
                type = detectEventUriType(url = uri, mimeType = mimeType),
                mimeType = mimeType,
            )
        }

fun List<ArticleData>.flatMapArticlesAsEventUriPO(
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
) = flatMap { articleData ->
    val uriAttachments = articleData.uris.map { uri ->
        EventIdUriPair(eventId = articleData.eventId, uri = uri)
    }

    val imageAttachment = articleData.imageCdnImage?.sourceUrl?.let { imageUrl ->
        listOf(EventIdUriPair(eventId = articleData.eventId, uri = imageUrl))
    } ?: emptyList()

    imageAttachment + uriAttachments
}
    .filterNot { it.uri.isNostrUri() }
    .mapToEventUri(
        cdnResources = cdnResources,
        linkPreviews = linkPreviews,
        videoThumbnails = videoThumbnails,
    )

private fun List<EventIdUriPair>.mapToEventUri(
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
): List<EventUri> =
    map { (eventId, uri) ->
        val uriCdnResource = cdnResources[uri]
        val linkPreview = linkPreviews[uri]
        val linkThumbnailCdnResource = linkPreview?.thumbnailUrl?.let { cdnResources[it] }
        val videoThumbnail = videoThumbnails[uri]
        val mimeType = uri.detectMimeType() ?: uriCdnResource?.contentType ?: linkPreview?.mimeType
        val type = detectEventUriType(url = uri, mimeType = mimeType)

        EventUri(
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

private fun detectEventUriType(url: String, mimeType: String?): EventUriType {
    mimeType?.let {
        val eventUriType = detectEventUriTypeByMimeType(mimeType)
        if (eventUriType != EventUriType.Other) {
            return eventUriType
        }
    }

    return detectEventUriTypeByUrl(url)
}

private fun detectEventUriTypeByMimeType(mimeType: String): EventUriType {
    return when {
        mimeType.startsWith("image") -> EventUriType.Image
        mimeType.startsWith("video") -> EventUriType.Video
        mimeType.startsWith("audio") -> EventUriType.Audio
        mimeType.endsWith("pdf") -> EventUriType.Pdf
        else -> EventUriType.Other
    }
}

private fun detectEventUriTypeByUrl(url: String): EventUriType {
    return when {
        url.contains(".youtube.com") -> EventUriType.YouTube
        url.contains("/youtube.com") -> EventUriType.YouTube
        url.contains("/youtu.be") -> EventUriType.YouTube
        url.contains(".rumble.com") || url.contains("/rumble.com") -> EventUriType.Rumble
        url.contains("/open.spotify.com/") -> EventUriType.Spotify
        url.contains("/listen.tidal.com/") -> EventUriType.Tidal
        url.contains("/github.com/") -> EventUriType.GitHub
        else -> EventUriType.Other
    }
}
