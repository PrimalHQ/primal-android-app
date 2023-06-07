package net.primal.android.nostr.ext

import net.primal.android.core.utils.detectContentType
import net.primal.android.feed.db.PostData
import net.primal.android.feed.db.PostResource

fun List<PostData>.flatMapAsPostResources() = flatMap { postData ->
    postData.urls.map { url ->
        PostResource(
            postId = postData.postId,
            contentType = url.detectContentType(),
            url = url,
        )
    }
}
