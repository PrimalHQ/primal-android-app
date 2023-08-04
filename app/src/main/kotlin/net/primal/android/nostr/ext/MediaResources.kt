package net.primal.android.nostr.ext

import net.primal.android.core.utils.detectContentType
import net.primal.android.feed.db.MediaResource
import net.primal.android.feed.db.PostData

fun List<PostData>.flatMapAsPostMediaResourcePO() = flatMap { postData ->
    postData.uris
        .filterNot { it.isNostrUri() }
        .map { url ->
            MediaResource(
                eventId = postData.postId,
                contentType = url.detectContentType(),
                url = url,
            )
        }
}
