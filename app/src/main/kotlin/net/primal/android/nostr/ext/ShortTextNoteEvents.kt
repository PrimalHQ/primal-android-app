package net.primal.android.nostr.ext

import com.linkedin.urls.detection.UrlDetector
import com.linkedin.urls.detection.UrlDetectorOptions
import net.primal.android.feed.db.PostData
import net.primal.android.nostr.model.NostrEvent

fun List<NostrEvent>.mapNotNullAsPost() = map { it.asPost() }

fun NostrEvent.asPost(): PostData = PostData(
    postId = this.id,
    authorId = this.pubKey,
    createdAt = this.createdAt,
    tags = this.tags,
    content = this.content,
    urls = this.content.parseUrls(),
    sig = this.sig,
)

private fun String.parseUrls(): List<String> {
    val links = UrlDetector(this, UrlDetectorOptions.Default).detect()
    return links.map { it.originalUrl }
}
