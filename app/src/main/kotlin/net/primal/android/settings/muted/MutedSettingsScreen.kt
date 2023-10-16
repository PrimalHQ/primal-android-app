package net.primal.android.settings.muted

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalOutlinedButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.settings.muted.model.MutedUser
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun MutedSettingsScreen(
    viewModel: MutedSettingsViewModel,
    onClose: () -> Unit
) {
    val state = viewModel.state.collectAsState()

    MutedSettingsScreen(
        state = state.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutedSettingsScreen(
    state: MutedSettingsContract.UiState,
    eventPublisher: (MutedSettingsContract.UiEvent) -> Unit,
    onClose: () -> Unit
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_muted_accounts_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                itemsIndexed(state.mutelist) { index, item ->
                    MutedUserItem(item = item, eventPublisher = eventPublisher)
                    if (index < state.mutelist.lastIndex) {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun MutedUserItem(
    item: MutedUser,
    eventPublisher: (MutedSettingsContract.UiEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.avatarUrl == null) {
            Image(
                modifier = Modifier.size(36.dp),
                painter = painterResource(id = R.drawable.default_avatar),
                contentDescription = null
            )
        } else {
            val model = ImageRequest.Builder(LocalContext.current).data(item.avatarUrl).build()
            AsyncImage(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                model = model,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1.0f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.name,
                fontWeight = FontWeight.W700,
                fontSize = 16.sp,
                lineHeight = 16.sp
            )
            Text(
                text = item.nip05InternetIdentifier ?: "",
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4
            )
        }
        PrimalOutlinedButton(
            modifier = Modifier.height(32.dp),
            onClick = {
            eventPublisher(MutedSettingsContract.UiEvent.UnmuteEvent(pubkey = item.pubkey))
        }) {
            Text(
                text = "Unmute",
                fontWeight = FontWeight.W500,
                fontSize = 12.sp
            )
        }
    }
}

@Preview
@Composable
fun PreviewMutedScreen() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        MutedSettingsScreen(
            state = MutedSettingsContract.UiState(
                mutelist = listOf(
                    MutedUser(
                        pubkey = "pubkey",
                        name = "username",
                        avatarUrl = null,
                        nip05InternetIdentifier = "nip05"
                    ),
                    MutedUser(
                        pubkey = "pubkey",
                        name = "username",
                        avatarUrl = "avatarUrl",
                        nip05InternetIdentifier = "nip05"
                    ),
                    MutedUser(
                        pubkey = "pubkey",
                        name = "username",
                        avatarUrl = null,
                        nip05InternetIdentifier = "nip05"
                    ),
                    MutedUser(
                        pubkey = "pubkey",
                        name = "username",
                        avatarUrl = null,
                        nip05InternetIdentifier = "nip05"
                    ), MutedUser(
                        pubkey = "pubkey",
                        name = "username",
                        avatarUrl = "avatarUrl",
                        nip05InternetIdentifier = "nip05"
                    )
                )
            ),
            eventPublisher = {},
            onClose = {})
    }
}