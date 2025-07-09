package net.primal.android.navigation.interactions

data class ContentInteractionCallbacks(
    val onProfileClick: (profileId: String) -> Unit,
    val onHashtagClick: (hashtag: String) -> Unit,
)
