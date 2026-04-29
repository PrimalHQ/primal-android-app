package net.primal.android.main.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.AppBarPage
import net.primal.domain.notifications.NotificationGroup

@Composable
fun NotificationGroup.toTitle(): String =
    when (this) {
        NotificationGroup.ALL -> stringResource(id = R.string.notifications_filter_all_title)
        NotificationGroup.ZAPS -> stringResource(id = R.string.notifications_filter_zaps_title)
        NotificationGroup.REPLIES -> stringResource(id = R.string.notifications_filter_replies_title)
        NotificationGroup.MENTIONS -> stringResource(id = R.string.notifications_filter_mentions_title)
        NotificationGroup.REPOSTS -> stringResource(id = R.string.notifications_filter_reposts_title)
    }

@Composable
fun NotificationGroup.toSubtitle(): String =
    when (this) {
        NotificationGroup.ALL -> stringResource(id = R.string.notifications_filter_all_description)
        NotificationGroup.ZAPS -> stringResource(id = R.string.notifications_filter_zaps_description)
        NotificationGroup.REPLIES -> stringResource(id = R.string.notifications_filter_replies_description)
        NotificationGroup.MENTIONS -> stringResource(id = R.string.notifications_filter_mentions_description)
        NotificationGroup.REPOSTS -> stringResource(id = R.string.notifications_filter_reposts_description)
    }

@Composable
fun List<NotificationGroup>.toAppBarPages(): List<AppBarPage> {
    val title = stringResource(id = R.string.notifications_title)
    return map { AppBarPage(title = title, subtitle = it.toTitle()) }
}
