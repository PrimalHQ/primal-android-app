package net.primal.android.premium.manage.media.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.time.format.FormatStyle
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.CopyAlt
import net.primal.android.core.compose.icons.primaliconpack.Delete
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromGallery
import net.primal.android.core.utils.formatToDefaultDateFormat
import net.primal.android.core.utils.toMegaBytes
import net.primal.android.theme.AppTheme

val DELETE_COLOR = Color(0xFFFA3C3C)

@Composable
fun MediaListItem(
    modifier: Modifier = Modifier,
    item: MediaUiItem,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(2.70f),
            contentAlignment = Alignment.CenterStart,
        ) {
            SubcomposeAsyncImage(
                modifier = Modifier
                    .size(width = 64.dp, height = 48.dp)
                    .clip(AppTheme.shapes.extraSmall),
                model = ImageRequest.Builder(context)
                    .data(item.thumbnailUrl ?: item.mediaUrl)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = { PlaceholderIcon(type = item.type) },
                error = { PlaceholderIcon(type = item.type) },
            )
        }
        MediaItemDetailsColumn(
            modifier = Modifier.weight(5f),
            item = item,
        )
        ActionIcon(
            modifier = Modifier.weight(2f),
            imageVector = PrimalIcons.CopyAlt,
            onClick = onCopyClick,
            tint = AppTheme.colorScheme.onPrimary,
        )
        ActionIcon(
            modifier = Modifier.weight(2f),
            imageVector = PrimalIcons.Delete,
            tint = DELETE_COLOR,
            onClick = onDeleteClick,
        )
    }
}

@Composable
private fun PlaceholderIcon(type: MediaType) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = when (type) {
                MediaType.Image -> PrimalIcons.ImportPhotoFromGallery
                MediaType.Video -> Icons.Filled.SmartDisplay
                MediaType.Other -> Icons.Filled.FilePresent
            },
            contentDescription = "",
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
    }
}

@Composable
private fun MediaItemDetailsColumn(modifier: Modifier, item: MediaUiItem) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val mediaType = when (item.type) {
            MediaType.Image -> stringResource(id = R.string.premium_media_management_table_type_image)
            MediaType.Video -> stringResource(id = R.string.premium_media_management_table_type_video)
            MediaType.Other -> stringResource(R.string.premium_media_management_table_type_other)
        }
        Text(
            text = stringResource(
                id = R.string.premium_media_management_media_size,
                item.sizeInBytes.toMegaBytes(),
            ) + " $mediaType",
            style = AppTheme.typography.bodyLarge,
        )
        Text(
            text = item.createdAt?.formatToDefaultDateFormat(FormatStyle.MEDIUM) ?: "",
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            style = AppTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ActionIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    onClick: () -> Unit,
    tint: Color = Color.Unspecified,
) {
    IconButton(
        onClick = onClick,
    ) {
        Icon(
            modifier = modifier.size(20.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
        )
    }
}

@Composable
fun TableHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(AppTheme.extraColorScheme.surfaceVariantAlt1)
            .padding(16.dp),
    ) {
        Text(
            modifier = Modifier.weight(3f),
            text = stringResource(id = R.string.premium_media_management_table_file),
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            modifier = Modifier.weight(5f),
            text = stringResource(id = R.string.premium_media_management_table_details),
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            modifier = Modifier.weight(2f),
            text = stringResource(id = R.string.premium_media_management_table_copy),
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
        Text(
            modifier = Modifier.weight(2f),
            text = stringResource(id = R.string.premium_media_management_table_delete),
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
    }
}
