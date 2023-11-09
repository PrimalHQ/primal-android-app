package net.primal.android.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.media.model.MediaResourceUi
import net.primal.android.core.ext.findByUrl

@Composable
fun AvatarThumbnail(
    modifier: Modifier = Modifier,
    authorAvatarUrl: String? = null,
    authorMediaResources: List<MediaResourceUi> = emptyList(),
    avatarSize: Dp = 48.dp,
    onClick: (() -> Unit)? = null
) {
    val resource = authorMediaResources.findByUrl(url = authorAvatarUrl)
    val variant = resource?.variants?.minByOrNull { it.width }
    val imageSource = variant?.mediaUrl ?: authorAvatarUrl
    AvatarThumbnailListItemImage(
        modifier = modifier,
        avatarSize = avatarSize,
        source = imageSource,
        onClick = onClick,
    )
}
