package net.primal.android.core.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

@Composable
fun AppIconThumbnail(
    modifier: Modifier = Modifier,
    avatarCdnImage: CdnImage?,
    appName: String?,
    avatarSize: Dp,
) {
    if (!avatarCdnImage?.sourceUrl.isNullOrBlank()) {
        val context = LocalContext.current

        PrimalAsyncImage(
            modifier = modifier
                .size(avatarSize)
                .clip(CircleShape),
            imageLoader = AvatarCoilImageLoader.provideNoGifsImageLoader(context = context),
            model = avatarCdnImage.sourceUrl,
            contentScale = ContentScale.Crop,
        )
    } else {
        val fallbackCharacter = appName?.firstOrNull()?.uppercase() ?: "?"
        Box(
            modifier = modifier
                .size(avatarSize)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = fallbackCharacter,
                style = AppTheme.typography.titleMedium,
                fontSize = (avatarSize.value / 2).sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }
    }
}
