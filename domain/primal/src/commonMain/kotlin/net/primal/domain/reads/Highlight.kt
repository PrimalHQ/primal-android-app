package net.primal.domain.reads

import net.primal.domain.posts.FeedPost
import net.primal.domain.profile.ProfileData

data class Highlight(
    val data: HighlightData,
    val author: ProfileData? = null,
    val comments: List<FeedPost> = emptyList(),
)
