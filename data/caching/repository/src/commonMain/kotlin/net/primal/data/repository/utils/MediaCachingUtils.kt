package net.primal.data.repository.utils

import net.primal.core.caching.MediaCacher
import net.primal.core.utils.asMapByKey
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.ContentMetadata
import net.primal.domain.nostr.NostrEvent

fun MediaCacher.cacheAvatarUrls(metadata: List<NostrEvent>, cdnResources: List<PrimalEvent>) {
    val avatarUrls = metadata.mapAsAvatarUrls(cdnResources = cdnResources)
    this.preCacheUserAvatars(urls = avatarUrls)
}

private fun List<NostrEvent>.mapAsAvatarUrls(cdnResources: List<PrimalEvent>): List<String> {
    val cdnMap = cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }

    return this.mapNotNull { event ->
        val metadata = event.content.decodeFromJsonStringOrNull<ContentMetadata>()
        val originalAvatarUrl = metadata?.picture

        if (originalAvatarUrl != null) {
            val cdnResource = cdnMap[originalAvatarUrl]
            val bestVariantUrl = cdnResource?.variants?.minByOrNull { it.width }?.mediaUrl

            bestVariantUrl ?: originalAvatarUrl
        } else {
            null
        }
    }
}
