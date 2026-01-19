package net.primal.data.repository.mappers.remote

import net.primal.core.utils.asMapByKey
import net.primal.core.utils.detectMimeType
import net.primal.data.local.dao.events.EventUri
import net.primal.data.local.dao.messages.DirectMessageData
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.reads.ArticleData
import net.primal.domain.links.CdnResource
import net.primal.domain.links.EventLink
import net.primal.domain.links.EventLinkPreviewData
import net.primal.domain.links.EventUriType
import net.primal.domain.nostr.extractDimension
import net.primal.domain.nostr.extractMimeType
import net.primal.domain.nostr.findIMetaTagForUrl
import net.primal.domain.nostr.utils.isNostrUri
import net.primal.shared.data.local.encryption.map

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
        postData.uris
            .filterNot { it.isNostrUri() }
            .map { uri ->
                postData.asEventUri(
                    url = uri,
                    cdnResources = cdnResources,
                    linkPreviews = linkPreviews,
                    videoThumbnails = videoThumbnails,
                )
            }
    }

private fun PostData.asEventUri(
    url: String,
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
): EventUri {
    val imetaTag = this.tags.findIMetaTagForUrl(url)
    val imetaDim = imetaTag?.extractDimension()
    val imetaMimeType = imetaTag?.extractMimeType()

    val cdnResource = cdnResources[url]
    val linkPreview = linkPreviews[url]

    val mimeType = resolveMimeType(
        url = url,
        cdnResource = cdnResource,
        linkPreview = linkPreview,
        imetaMimeType = imetaMimeType,
    )

    val type = detectEventUriType(url = url, mimeType = mimeType)

    return EventUri(
        eventId = this.postId,
        url = url,
        type = type,
        mimeType = mimeType,
        variants = resolveVariants(cdnResource, linkPreview, cdnResources),
        title = linkPreview?.title?.ifBlank { null },
        description = linkPreview?.description?.ifBlank { null },
        thumbnail = linkPreview?.thumbnailUrl?.ifBlank { null } ?: videoThumbnails[url],
        authorAvatarUrl = linkPreview?.authorAvatarUrl?.ifBlank { null },
        originalWidth = imetaDim?.first,
        originalHeight = imetaDim?.second,
    )
}

private fun resolveMimeType(
    url: String,
    cdnResource: CdnResource?,
    linkPreview: EventLinkPreviewData?,
    imetaMimeType: String?,
): String? {
    return url.detectMimeType()
        ?: cdnResource?.contentType
        ?: linkPreview?.mimeType
        ?: imetaMimeType
}

private fun resolveVariants(
    cdnResource: CdnResource?,
    linkPreview: EventLinkPreviewData?,
    allCdnResources: Map<String, CdnResource>,
) = (cdnResource?.variants ?: emptyList()) +
    (linkPreview?.thumbnailUrl?.let { allCdnResources[it] }?.variants ?: emptyList())

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
            originalWidth = eventUri.originalWidth,
            originalHeight = eventUri.originalHeight,
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
    cdnResources: List<CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
): List<EventUri> {
    val cdnResourcesByUrl = cdnResources.asMapByKey { it.url }
    return flatMapArticlesAsEventUriPO(
        cdnResources = cdnResourcesByUrl,
        linkPreviews = linkPreviews,
        videoThumbnails = videoThumbnails,
    )
}

fun List<ArticleData>.flatMapArticlesAsEventUriPO(
    cdnResources: Map<String, CdnResource>,
    linkPreviews: Map<String, EventLinkPreviewData>,
    videoThumbnails: Map<String, String>,
): List<EventUri> =
    flatMap { articleData ->
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
