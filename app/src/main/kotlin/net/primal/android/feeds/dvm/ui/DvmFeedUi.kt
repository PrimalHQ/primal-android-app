package net.primal.android.feeds.dvm.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.feeds.domain.DvmFeed

@Serializable
data class DvmFeedUi(
    val data: DvmFeed,
    val userLiked: Boolean? = false,
    val userZapped: Boolean? = false,
    val totalLikes: Long? = null,
    val totalSatsZapped: Long? = null,
    val actionUserAvatars: List<CdnImage> = emptyList(),
)

val DvmFeedUiSaver = Saver<MutableState<DvmFeedUi?>, String>(
    save = {
        NostrJson.encodeToString(it.component1())
    },
    restore = {
        mutableStateOf(NostrJson.decodeFromString(it))
    }
)
