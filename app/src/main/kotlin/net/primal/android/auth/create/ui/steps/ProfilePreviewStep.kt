package net.primal.android.auth.create.ui.steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import net.primal.android.R
import net.primal.android.auth.create.CreateAccountContract
import net.primal.android.theme.AppTheme

@Composable
fun ProfilePreviewStep(state: CreateAccountContract.UiState, isFinalized: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(
            modifier = Modifier
                .height(336.dp)
                .fillMaxWidth()
                .padding(32.dp)
                .clip(RoundedCornerShape(size = 12.dp))
                .border(
                    width = 1.dp,
                    color = if (isFinalized) AppTheme.extraColorScheme.successBright else Color.White,
                    shape = RoundedCornerShape(size = 12.dp),
                ),
        ) {
            if (state.bannerUri != null) {
                val model = ImageRequest.Builder(LocalContext.current).data(state.bannerUri).build()
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(102.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(102.dp)
                        .background(color = Color(0xFF181818)),
                )
            }

            Box(
                modifier = Modifier
                    .offset(y = (-36).dp)
                    .padding(horizontal = 16.dp)
                    .size(size = 78.dp)
                    .clip(shape = CircleShape)
                    .background(color = Color.Black)
                    .align(Alignment.CenterStart),
            ) {
                if (state.avatarUri != null) {
                    val model =
                        ImageRequest.Builder(LocalContext.current).data(state.avatarUri).build()

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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
                    .align(alignment = Alignment.BottomCenter),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = state.displayName,
                        fontWeight = FontWeight.W700,
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        color = AppTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "@${state.username}",
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    )
                }
                Text(
                    text = state.aboutMe,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = AppTheme.colorScheme.onPrimary,
                )
                Text(
                    text = state.website,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = AppTheme.colorScheme.primary,
                )
            }
        }
        if (isFinalized) {
            Text(
                text = stringResource(id = R.string.create_success_subtitle),
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.41.sp,
                color = AppTheme.extraColorScheme.successBright,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-32).dp),
            )
        }
        if (isFinalized) {
            Row(
                modifier = Modifier
                    .padding(start = 32.dp, end = 32.dp)
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        color = Color(0xFF181818),
                        shape = RoundedCornerShape(size = 12.dp),
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.key),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Text(
                    text = stringResource(id = R.string.create_finish_subtitle),
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = AppTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(end = 24.dp),
                )
            }
        } else {
            Text(
                text = stringResource(id = R.string.create_subtitle_profile_preview),
                modifier = Modifier.padding(horizontal = 32.dp),
                fontWeight = FontWeight.W400,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        }
    }
}
