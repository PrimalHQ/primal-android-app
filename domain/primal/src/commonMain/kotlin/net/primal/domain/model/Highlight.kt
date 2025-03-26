package net.primal.domain.model

data class Highlight(
    val data: HighlightData,
    val author: ProfileData? = null,
    val comments: List<FeedPost> = emptyList(),
)
