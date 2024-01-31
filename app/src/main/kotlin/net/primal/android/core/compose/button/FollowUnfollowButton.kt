package net.primal.android.core.compose.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun FollowUnfollowButton(
    isFollowed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimalFilledButton(
        modifier = modifier,
        containerColor = if (isFollowed) {
            AppTheme.extraColorScheme.surfaceVariantAlt1
        } else {
            AppTheme.colorScheme.onSurface
        },
        contentColor = if (isFollowed) {
            AppTheme.colorScheme.onSurface
        } else {
            AppTheme.colorScheme.surface
        },
        textStyle = AppTheme.typography.titleMedium.copy(
            lineHeight = 14.sp,
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        onClick = onClick,
    ) {
        val text = if (isFollowed) {
            stringResource(id = R.string.create_recommended_unfollow)
        } else {
            stringResource(id = R.string.create_recommended_follow)
        }
        Text(text = text.lowercase())
    }
}
