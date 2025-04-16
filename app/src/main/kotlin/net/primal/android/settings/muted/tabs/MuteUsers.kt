package net.primal.android.settings.muted.tabs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.settings.muted.MutedSettingsContract
import net.primal.android.theme.AppTheme

@Composable
fun MuteUsers(
    state: MutedSettingsContract.UiState,
    eventPublisher: (MutedSettingsContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: (String) -> Unit,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding(),
    ) {
        items(
            items = state.mutedUsers,
            key = { it.pubkey },
            contentType = { "MutedUser" },
        ) { mutedUser ->
            MutedUserListItem(
                item = mutedUser,
                onUnmuteClick = {
                    eventPublisher(
                        MutedSettingsContract.UiEvent.UnmuteUser(mutedUser.pubkey),
                    )
                },
                onProfileClick = onProfileClick,
            )
            PrimalDivider()
        }

        if (state.mutedUsers.isEmpty()) {
            item(contentType = "NoContent") {
                ListNoContent(
                    modifier = Modifier.fillParentMaxSize(),
                    noContentText = stringResource(
                        id = R.string.settings_muted_accounts_no_content,
                    ),
                    refreshButtonVisible = false,
                )
            }
        }
    }
}

@Composable
fun MutedUserListItem(
    item: ProfileDetailsUi,
    onUnmuteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = {
            UniversalAvatarThumbnail(
                avatarCdnImage = item.avatarCdnImage,
                onClick = { onProfileClick(item.pubkey) },
                legendaryCustomization = item.premiumDetails?.legendaryCustomization,
            )
        },
        headlineContent = {
            NostrUserText(
                displayName = item.authorDisplayName,
                fontSize = 14.sp,
                internetIdentifier = item.internetIdentifier,
                legendaryCustomization = item.premiumDetails?.legendaryCustomization,
            )
        },
        supportingContent = {
            if (!item.internetIdentifier.isNullOrEmpty()) {
                Text(
                    text = item.internetIdentifier,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    fontSize = 14.sp,
                )
            }
        },
        trailingContent = {
            PrimalFilledButton(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(36.dp),
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                contentColor = AppTheme.colorScheme.onSurface,
                textStyle = AppTheme.typography.titleMedium.copy(
                    lineHeight = 18.sp,
                ),
                onClick = { onUnmuteClick(item.pubkey) },
            ) {
                Text(
                    text = stringResource(
                        id = R.string.settings_muted_accounts_unmute_button,
                    ).lowercase(),
                )
            }
        },
    )
}
