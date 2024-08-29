package net.primal.android.feeds.ui.model

data class FeedUi(
    val directive: String,
    val name: String,
    val description: String,
    val enabled: Boolean = true,
    val deletable: Boolean = true,
)
