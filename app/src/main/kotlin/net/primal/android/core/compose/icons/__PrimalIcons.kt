package net.primal.android.core.compose.icons

import androidx.compose.ui.graphics.vector.ImageVector
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.AvatarNostrich
import net.primal.android.core.compose.icons.primaliconpack.Delete
import net.primal.android.core.compose.icons.primaliconpack.Discuss
import net.primal.android.core.compose.icons.primaliconpack.Download
import net.primal.android.core.compose.icons.primaliconpack.Edit
import net.primal.android.core.compose.icons.primaliconpack.Explore
import net.primal.android.core.compose.icons.primaliconpack.FeedLikes
import net.primal.android.core.compose.icons.primaliconpack.FeedLikesFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedPicker
import net.primal.android.core.compose.icons.primaliconpack.FeedReplies
import net.primal.android.core.compose.icons.primaliconpack.FeedRepliesFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedReposts
import net.primal.android.core.compose.icons.primaliconpack.FeedRepostsFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedZaps
import net.primal.android.core.compose.icons.primaliconpack.FeedZapsFilled
import net.primal.android.core.compose.icons.primaliconpack.Help
import net.primal.android.core.compose.icons.primaliconpack.Home
import net.primal.android.core.compose.icons.primaliconpack.Messages
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.compose.icons.primaliconpack.Notifications
import net.primal.android.core.compose.icons.primaliconpack.Read
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.compose.icons.primaliconpack.Settings
import net.primal.android.core.compose.icons.primaliconpack.Verified
import kotlin.collections.List as ____KtList

object PrimalIcons

private var __PrimalIcons: ____KtList<ImageVector>? = null

val PrimalIcons.PrimalIcons: ____KtList<ImageVector>
    get() {
        if (__PrimalIcons != null) {
            return __PrimalIcons!!
        }
        __PrimalIcons = listOf(
            AvatarDefault,
            AvatarNostrich,
            Search,
            Home,
            FeedRepostsFilled,
            FeedLikesFilled,
            FeedPicker,
            FeedZaps,
            FeedZapsFilled,
            FeedLikes,
            Settings,
            Download,
            FeedRepliesFilled,
            FeedReplies,
            Messages,
            Explore,
            More,
            Edit,
            Discuss,
            Delete,
            Notifications,
            Read,
            Help,
            FeedReposts,
            Verified
        )
        return __PrimalIcons!!
    }
