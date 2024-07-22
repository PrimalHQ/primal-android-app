package net.primal.android.core.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterial3Api
@Composable
fun PrimalTopAppBar(
    modifier: Modifier = Modifier,
    title: String = "",
    subtitle: String? = null,
    titleTrailingIcon: ImageVector? = null,
    textColor: Color = LocalContentColor.current,
    navigationIcon: ImageVector? = null,
    navigationIconTintColor: Color = LocalContentColor.current,
    navigationIconContentDescription: String? = null,
    onNavigationIconClick: (() -> Unit)? = null,
    autoCloseKeyboardOnNavigationIconClick: Boolean = true,
    avatarCdnImage: CdnImage? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    showDivider: Boolean = true,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onTitleClick: (() -> Unit)? = null,
    onTitleLongClick: (() -> Unit)? = null,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = AppTheme.colorScheme.surface,
        scrolledContainerColor = AppTheme.colorScheme.surface,
    ),
    footer: @Composable () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier,
    ) {
        CenterAlignedTopAppBar(
            navigationIcon = {
                if (avatarCdnImage != null) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(CircleShape),
                    ) {
                        AvatarThumbnail(
                            avatarCdnImage = avatarCdnImage,
                            modifier = Modifier.size(32.dp),
                            onClick = onNavigationIconClick,
                        )
                    }
                } else if (navigationIcon != null) {
                    AppBarIcon(
                        icon = navigationIcon,
                        onClick = {
                            if (onNavigationIconClick != null) {
                                if (autoCloseKeyboardOnNavigationIconClick) {
                                    keyboardController?.hide()
                                }
                                onNavigationIconClick()
                            }
                        },
                        tint = navigationIconTintColor,
                        appBarIconContentDescription = navigationIconContentDescription,
                    )
                } else {
                    InvisibleAppBarIcon()
                }
            },
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onTitleClick?.invoke() },
                            onLongClick = onTitleLongClick,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    IconText(
                        modifier = Modifier.padding(start = 20.dp),
                        text = title,
                        color = textColor,
                        trailingIcon = if (title.isNotEmpty()) titleTrailingIcon else null,
                        trailingIconTintColor = textColor,
                        iconSize = 20.sp,
                    )
                    if (subtitle?.isNotBlank() == true) {
                        Text(
                            text = subtitle,
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                        )
                    }
                }
            },
            actions = actions ?: { InvisibleAppBarIcon() },
            colors = colors,
            scrollBehavior = scrollBehavior,
        )

        Box(
            modifier = Modifier
                .background(color = AppTheme.colorScheme.surface)
                .fillMaxWidth(),
        ) {
            footer()
        }

        if (showDivider) {
            PrimalDivider()
        }
    }
}

@Composable
fun InvisibleAppBarIcon() {
    AppBarIcon(
        modifier = Modifier.alpha(0.0f),
        icon = Icons.AutoMirrored.Outlined.ArrowForward,
        onClick = { },
        enabled = false,
    )
}
