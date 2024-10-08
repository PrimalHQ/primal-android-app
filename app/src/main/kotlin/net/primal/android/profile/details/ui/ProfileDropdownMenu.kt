package net.primal.android.profile.details.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.core.compose.dropdown.DropdownPrimalMenuItem
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ContextMuteUser
import net.primal.android.core.compose.icons.primaliconpack.ContextReportUser
import net.primal.android.core.compose.icons.primaliconpack.ContextShare
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.compose.icons.primaliconpack.UserFeedAdd
import net.primal.android.core.utils.resolvePrimalProfileLink
import net.primal.android.core.utils.systemShareText
import net.primal.android.profile.details.ProfileDetailsContract
import net.primal.android.profile.report.ReportUserDialog
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun ProfileDropdownMenu(
    profileId: String,
    profileName: String,
    isActiveUser: Boolean,
    isProfileMuted: Boolean,
    isProfileFeedInActiveUserFeeds: Boolean,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
) {
    val context = LocalContext.current
    var menuVisible by remember { mutableStateOf(false) }

    var reportDialogVisible by remember { mutableStateOf(false) }
    if (reportDialogVisible) {
        ReportUserDialog(
            onDismissRequest = { reportDialogVisible = false },
            onReportClick = {
                reportDialogVisible = false
                eventPublisher(
                    ProfileDetailsContract.UiEvent.ReportAbuse(
                        type = it,
                        profileId = profileId,
                    ),
                )
            },
        )
    }

    AppBarIcon(
        icon = PrimalIcons.More,
        onClick = { menuVisible = true },
        appBarIconContentDescription = stringResource(id = R.string.accessibility_profile_drop_down),
    )

    DropdownPrimalMenu(
        expanded = menuVisible,
        onDismissRequest = { menuVisible = false },
    ) {
        if (!isActiveUser) {
            AddOrRemoveUserFeedMenuItem(
                profileId = profileId,
                profileName = profileName,
                isProfileFeedInActiveUserFeeds = isProfileFeedInActiveUserFeeds,
                eventPublisher = eventPublisher,
                onDismiss = { menuVisible = false },
            )
        }

        DropdownPrimalMenuItem(
            trailingIconVector = PrimalIcons.ContextShare,
            text = stringResource(id = R.string.profile_context_share_profile),
            onClick = {
                systemShareText(context = context, text = resolvePrimalProfileLink(profileId = profileId))
                menuVisible = false
            },
        )

        if (!isActiveUser) {
            MuteOrUnmuteProfileMenuItem(
                profileId = profileId,
                isProfileMuted = isProfileMuted,
                eventPublisher = eventPublisher,
                onDismiss = { menuVisible = false },
            )

            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.ContextReportUser,
                tint = AppTheme.colorScheme.error,
                text = stringResource(id = R.string.context_menu_report_user),
                onClick = {
                    reportDialogVisible = true
                    menuVisible = false
                },
            )
        }
    }
}

@Composable
private fun MuteOrUnmuteProfileMenuItem(
    profileId: String,
    isProfileMuted: Boolean,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownPrimalMenuItem(
        trailingIconVector = PrimalIcons.ContextMuteUser,
        text = if (isProfileMuted) {
            stringResource(id = R.string.context_menu_unmute_user)
        } else {
            stringResource(id = R.string.context_menu_mute_user)
        },
        tint = AppTheme.colorScheme.error,
        onClick = {
            eventPublisher(
                if (isProfileMuted) {
                    ProfileDetailsContract.UiEvent.UnmuteAction(profileId = profileId)
                } else {
                    ProfileDetailsContract.UiEvent.MuteAction(profileId = profileId)
                },
            )
            onDismiss()
        },
    )
}

@Composable
private fun AddOrRemoveUserFeedMenuItem(
    profileId: String,
    profileName: String,
    isProfileFeedInActiveUserFeeds: Boolean,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val title = stringResource(id = R.string.profile_save_user_feed_title, profileName)
    val description = stringResource(id = R.string.profile_save_user_feed_description, profileName)

    DropdownPrimalMenuItem(
        trailingIconVector = PrimalIcons.UserFeedAdd,
        text = if (isProfileFeedInActiveUserFeeds) {
            stringResource(id = R.string.profile_context_remove_user_feed)
        } else {
            stringResource(id = R.string.profile_context_add_user_feed)
        },
        onClick = {
            if (isProfileFeedInActiveUserFeeds) {
                eventPublisher(ProfileDetailsContract.UiEvent.RemoveProfileFeedAction(profileId = profileId))
            } else {
                eventPublisher(
                    ProfileDetailsContract.UiEvent.AddProfileFeedAction(
                        profileId = profileId,
                        feedTitle = title,
                        feedDescription = description,
                    ),
                )
            }
            onDismiss()
        },
    )
}
