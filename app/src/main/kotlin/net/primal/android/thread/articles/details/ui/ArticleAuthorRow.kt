package net.primal.android.thread.articles.details.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.premium.legend.LegendaryStyle
import net.primal.android.theme.AppTheme

@Composable
fun ArticleAuthorRow(
    modifier: Modifier,
    authorFollowed: Boolean,
    authorDisplayName: String,
    authorCdnImage: CdnImage? = null,
    authorInternetIdentifier: String? = null,
    authorLegendaryCustomization: LegendaryCustomization? = null,
    onAuthorAvatarClick: (() -> Unit)? = null,
    onFollowUnfollowClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UniversalAvatarThumbnail(
            avatarSize = 42.dp,
            avatarCdnImage = authorCdnImage,
            onClick = onAuthorAvatarClick,
            legendaryCustomization = authorLegendaryCustomization,
        )

        Column(
            modifier = Modifier
                .weight(weight = 1.0f)
                .padding(horizontal = 12.dp),
        ) {
            NostrUserText(
                displayName = authorDisplayName,
                internetIdentifier = authorInternetIdentifier,
                overflow = TextOverflow.Ellipsis,
                style = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                ),
                legendaryCustomization = authorLegendaryCustomization,
            )

            if (!authorInternetIdentifier.isNullOrBlank()) {
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = authorInternetIdentifier,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    style = AppTheme.typography.bodySmall,
                )
            }
        }

        if (!authorFollowed) {
            FollowButton(onClick = { onFollowUnfollowClick?.invoke() })
        } else {
            var showUnfollowPrompt by remember { mutableStateOf(false) }
            if (showUnfollowPrompt) {
                UnfollowAuthor(
                    authorDisplayName = authorDisplayName.trim(),
                    onDismissRequest = { showUnfollowPrompt = false },
                    onConfirm = {
                        showUnfollowPrompt = false
                        onFollowUnfollowClick?.invoke()
                    },
                )
            }

            FollowingButton(onClick = { showUnfollowPrompt = true })
        }
    }
}

@Composable
private fun UnfollowAuthor(
    authorDisplayName: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(
                    id = R.string.article_author_confirm_unfollow_text,
                    authorDisplayName,
                ),
            )
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(id = R.string.article_author_dismiss_unfollow_button),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(id = R.string.article_author_confirm_unfollow_button),
                )
            }
        },
    )
}

@Composable
private fun FollowButton(onClick: () -> Unit) {
    ProfileButton(
        text = stringResource(id = R.string.article_author_follow_button),
        containerColor = AppTheme.colorScheme.onSurface,
        contentColor = AppTheme.colorScheme.surface,
        onClick = onClick,
    )
}

@Composable
private fun FollowingButton(onClick: () -> Unit) {
    ProfileButton(
        text = stringResource(id = R.string.article_author_following_button),
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
        textStyle = AppTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.SemiBold,
        ),
        onClick = onClick,
    ) {
        Text(text = text)
    }
}

@Preview
@Composable
fun PreviewArticleAuthorRow() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            ArticleAuthorRow(
                modifier = Modifier.fillMaxWidth(),
                authorFollowed = true,
                authorDisplayName = "miljan",
                authorInternetIdentifier = "miljan@primal.net",
            )
        }
    }
}

@Preview
@Composable
fun PreviewArticleLegendaryAuthorRow() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            ArticleAuthorRow(
                modifier = Modifier.fillMaxWidth(),
                authorFollowed = true,
                authorDisplayName = "miljan",
                authorInternetIdentifier = "miljan@primal.net",
                authorLegendaryCustomization = LegendaryCustomization(
                    avatarGlow = true,
                    customBadge = true,
                    legendaryStyle = LegendaryStyle.SUN_FIRE,
                ),
            )
        }
    }
}
