package net.primal.android.profile.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedZaps
import net.primal.android.core.compose.icons.primaliconpack.Message
import net.primal.android.core.compose.icons.primaliconpack.QrCode
import net.primal.android.theme.AppTheme

@Composable
fun ProfileActions(
    modifier: Modifier,
    isFollowed: Boolean,
    isActiveUser: Boolean,
    onEditProfileClick: () -> Unit,
    onMessageClick: () -> Unit,
    onZapProfileClick: () -> Unit,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
    ) {
        ActionButton(
            onClick = { },
            iconVector = PrimalIcons.QrCode,
            iconPadding = 11.dp,
            contentDescription = stringResource(id = R.string.accessibility_profile_qr_code),
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (!isActiveUser) {
            ActionButton(
                onClick = onZapProfileClick,
                iconVector = PrimalIcons.FeedZaps,
                contentDescription = stringResource(id = R.string.accessibility_profile_send_zap),
            )

            Spacer(modifier = Modifier.width(8.dp))
        }

        ActionButton(
            onClick = onMessageClick,
            iconVector = PrimalIcons.Message,
            iconPadding = 3.dp,
            contentDescription = stringResource(id = R.string.accessibility_profile_messages),
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (!isActiveUser) {
            when (isFollowed) {
                true -> UnfollowButton(onClick = onUnfollow)
                false -> FollowButton(onClick = onFollow)
            }
        } else {
            EditProfileButton(onClick = { onEditProfileClick() })
        }
    }
}

@Composable
private fun ActionButton(
    iconVector: ImageVector,
    onClick: () -> Unit,
    iconPadding: Dp = 2.dp,
    contentDescription: String? = null,
) {
    IconButton(
        modifier = Modifier.size(40.dp),
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            contentColor = AppTheme.colorScheme.onSurface,
        ),
    ) {
        Icon(
            modifier = Modifier.padding(all = iconPadding),
            imageVector = iconVector,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun FollowButton(onClick: () -> Unit) {
    ProfileButton(
        text = stringResource(id = R.string.profile_follow_button).lowercase(),
        containerColor = AppTheme.colorScheme.onSurface,
        contentColor = AppTheme.colorScheme.surface,
        onClick = onClick,
    )
}

@Composable
private fun UnfollowButton(onClick: () -> Unit) {
    ProfileButton(
        text = stringResource(id = R.string.profile_unfollow_button).lowercase(),
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        contentColor = AppTheme.colorScheme.onSurface,
        onClick = onClick,
    )
}

@Composable
private fun EditProfileButton(onClick: () -> Unit) {
    ProfileButton(
        text = stringResource(id = R.string.profile_edit_profile_button).lowercase(),
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        contentColor = AppTheme.colorScheme.onSurface,
        onClick = onClick,
    )
}

@Composable
private fun ProfileButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    PrimalFilledButton(
        modifier = Modifier
            .height(40.dp)
            .wrapContentWidth()
            .defaultMinSize(minWidth = 108.dp),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 0.dp,
        ),
        containerColor = containerColor,
        contentColor = contentColor,
        textStyle = AppTheme.typography.bodyMedium.copy(
            fontSize = 18.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        onClick = onClick,
    ) {
        Text(text = text)
    }
}
