package net.primal.android.nostr.ext

import net.primal.android.core.utils.detectContentType
import net.primal.android.feed.db.PostData
import net.primal.android.feed.db.MediaResource

fun List<PostData>.flatMapAsPostResources() = flatMap { postData ->
    postData.uris
        .filter {
            !it.isNostrUri()
        }
        .map { url ->
        MediaResource(
            eventId = postData.postId,
            contentType = url.detectContentType(),
            url = url,
        )
    }
}
