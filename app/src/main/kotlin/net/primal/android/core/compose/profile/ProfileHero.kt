package net.primal.android.core.compose.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun ProfileHero(
    bannerUri: Uri?,
    avatarUri: Uri?,
    onBannerUriChange: (Uri?) -> Unit,
    onAvatarUriChange: (Uri?) -> Unit,
) {
    val avatarPickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            onAvatarUriChange(uri)
        }
    val bannerPickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            onBannerUriChange(uri)
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .height(179.dp),
    ) {
        if (bannerUri != null) {
            val model = ImageRequest.Builder(LocalContext.current).data(bannerUri).build()
            AsyncImage(
                model = model,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp)
                    .background(
                        color = AppTheme.extraColorScheme.surfaceVariantAlt3,
                    ),
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(size = 108.dp)
                .clip(shape = CircleShape)
                .background(color = Color.Black)
                .align(Alignment.BottomStart),
        ) {
            if (avatarUri != null) {
                val model = ImageRequest.Builder(LocalContext.current).data(avatarUri).build()

                AsyncImage(
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_avatar),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp)
                .padding(end = 32.dp)
                .height(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    id = if (avatarUri != null) R.string.create_change_avatar else R.string.create_set_avatar,
                ),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    avatarPickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                color = AppTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Divider(
                modifier = Modifier
                    .height(16.dp)
                    .width(1.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(
                    id = if (bannerUri != null) R.string.create_change_banner else R.string.create_set_banner,
                ),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    bannerPickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                color = AppTheme.colorScheme.secondary,
            )
        }
    }
}
