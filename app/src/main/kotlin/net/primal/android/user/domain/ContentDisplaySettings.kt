package net.primal.android.user.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentDisplaySettings(
    val autoPlayVideos: Int = AUTO_PLAY_VIDEO_NEVER,
    val showAnimatedAvatars: Boolean = false,
    val focusModeEnabled: Boolean = true,
    @SerialName("noteAppearance") val contentAppearance: ContentAppearance = ContentAppearance.Default,
) {
    companion object {
        const val AUTO_PLAY_VIDEO_NEVER = 0
        const val AUTO_PLAY_VIDEO_ALWAYS = 1
    }
}
