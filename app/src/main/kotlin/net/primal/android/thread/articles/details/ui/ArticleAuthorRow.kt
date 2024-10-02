package net.primal.android.thread.articles.details.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme

@Composable
fun ArticleAuthorRow(
    modifier: Modifier,
    authorDisplayName: String,
    authorCdnImage: CdnImage? = null,
    authorInternetIdentifier: String? = null,
    onAuthorAvatarClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnail(
            avatarSize = 42.dp,
            avatarCdnImage = authorCdnImage,
            onClick = onAuthorAvatarClick,
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

        PrimalLoadingButton(
            text = "Subscribe",
            height = 40.dp,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            contentPadding = PaddingValues(horizontal = 18.dp),
            onClick = {
            },
        )
    }
}

@Preview
@Composable
fun PreviewArticleAuthorRow() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            ArticleAuthorRow(
                modifier = Modifier.fillMaxWidth(),
                authorDisplayName = "miljan",
                authorInternetIdentifier = "miljan@primal.net",
            )
        }
    }
}
