package net.primal.android.explore.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.button.FollowUnfollowButton
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.core.utils.shortened
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun UserProfileListItem(
    data: UserProfileItemUi,
    onClick: (String) -> Unit,
    showFollowUnfollow: Boolean = false,
    isFollowed: Boolean = false,
    onFollowUnfollowClick: (() -> Unit)? = null,
) {
    ListItem(
        modifier = Modifier.clickable { onClick(data.profileId) },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = {
            AvatarThumbnail(
                avatarCdnImage = data.avatarCdnImage,
                onClick = { onClick(data.profileId) },
            )
        },
        headlineContent = {
            Text(
                text = data.displayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            if (!data.internetIdentifier.isNullOrEmpty()) {
                Text(
                    text = data.internetIdentifier.formatNip05Identifier(),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (data.followersCount != null) {
                    Column(
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = data.followersCount.shortened(),
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Bold,
                            style = AppTheme.typography.bodyMedium,
                        )
                        Text(
                            text = stringResource(id = R.string.search_followers_text).lowercase(),
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                        )
                    }
                }

                if (showFollowUnfollow) {
                    Spacer(modifier = Modifier.width(8.dp))
                    FollowUnfollowButton(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 92.dp)
                            .height(36.dp),
                        isFollowed = isFollowed,
                        onClick = { onFollowUnfollowClick?.invoke() },
                    )
                }
            }
        },
    )
}

@Preview
@Composable
fun PreviewUserProfileListItemWithFollow() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            UserProfileListItem(
                data = UserProfileItemUi(
                    profileId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
                    displayName = "alex",
                    internetIdentifier = "alex@primal.net",
                    followersCount = 12345,
                ),
                showFollowUnfollow = true,
                isFollowed = false,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewUserProfileListItemWithUnfollow() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            UserProfileListItem(
                data = UserProfileItemUi(
                    profileId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
                    displayName = "alex",
                    internetIdentifier = "alex@primal.net",
                    followersCount = 12345,
                ),
                showFollowUnfollow = true,
                isFollowed = true,
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewUserProfileListItem() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            UserProfileListItem(
                data = UserProfileItemUi(
                    profileId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
                    displayName = "alex",
                    internetIdentifier = "alex@primal.net",
                    followersCount = 12345,
                ),
                showFollowUnfollow = false,
                isFollowed = true,
                onClick = {},
            )
        }
    }
}
