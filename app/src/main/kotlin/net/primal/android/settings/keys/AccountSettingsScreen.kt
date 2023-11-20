package net.primal.android.settings.keys

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Key
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun AccountSettingsScreen(viewModel: AccountSettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    AccountSettingsScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(state: AccountSettingsContract.UiState, onClose: () -> Unit) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_keys_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            ) {
                item {
                    PublicKeySection(
                        npub = state.npub,
                        avatarCdnImage = state.avatarCdnImage,
                    )
                }

                item {
                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = (0.5).dp,
                    )
                }

                item {
                    PrivateKeySection(
                        nsec = state.nsec,
                    )
                }
            }
        },
    )
}

@Composable
fun PublicKeySection(npub: String, avatarCdnImage: CdnImage?) {
    val context = LocalContext.current

    Text(
        modifier = Modifier.padding(vertical = 8.dp),
        text = stringResource(id = R.string.settings_keys_public_key_title).uppercase(),
        style = AppTheme.typography.bodySmall,
        color = AppTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
    )

    Row(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .height(72.dp)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.medium,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.padding(start = 16.dp)) {
            AvatarThumbnail(avatarCdnImage = avatarCdnImage)
        }
        Text(
            modifier = Modifier.padding(all = 16.dp),
            text = npub,
            style = AppTheme.typography.bodySmall,
            maxLines = 2,
        )
    }

    Box(modifier = Modifier.padding(vertical = 8.dp)) {
        var keyCopied by remember { mutableStateOf(false) }
        PrimalLoadingButton(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            leadingIcon = if (keyCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
            text = if (keyCopied) {
                stringResource(id = R.string.settings_keys_key_copied)
            } else {
                stringResource(id = R.string.settings_keys_copy_public_key)
            },
            onClick = {
                val clipboard = context.getSystemService(ClipboardManager::class.java)
                val clip = ClipData.newPlainText("", npub)
                clipboard.setPrimaryClip(clip)
                keyCopied = true
            },
        )
    }

    Text(
        modifier = Modifier.padding(vertical = 8.dp),
        text = stringResource(id = R.string.settings_keys_public_key_hint),
        style = AppTheme.typography.bodySmall,
        lineHeight = 20.sp,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
    )
}

@Composable
fun PrivateKeySection(nsec: String) {
    val context = LocalContext.current
    var privateKeyVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.settings_keys_private_key_title).uppercase(),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable {
                    privateKeyVisible = !privateKeyVisible
                },
            text = if (privateKeyVisible) {
                stringResource(id = R.string.settings_keys_hide_key)
            } else {
                stringResource(id = R.string.settings_keys_show_key)
            }.lowercase(),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium,
        )
    }

    Row(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
            .height(72.dp)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.medium,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.padding(start = 16.dp)) {
            Icon(
                modifier = Modifier
                    .size(50.dp)
                    .padding(all = 8.dp),
                imageVector = PrimalIcons.Key,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }
        Text(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxWidth(),
            text = if (privateKeyVisible) nsec else "• ".repeat(nsec.length),
            style = if (privateKeyVisible) AppTheme.typography.bodySmall else AppTheme.typography.titleLarge,
            textAlign = if (privateKeyVisible) TextAlign.Start else TextAlign.Center,
            maxLines = if (privateKeyVisible) 2 else 1,
            overflow = if (privateKeyVisible) TextOverflow.Ellipsis else TextOverflow.Clip,
        )
    }

    Box(modifier = Modifier.padding(vertical = 8.dp)) {
        var keyCopied by remember { mutableStateOf(false) }
        PrimalLoadingButton(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            leadingIcon = if (keyCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
            text = if (keyCopied) {
                stringResource(id = R.string.settings_keys_key_copied)
            } else {
                stringResource(id = R.string.settings_keys_copy_private_key)
            },
            onClick = {
                val clipboard = context.getSystemService(ClipboardManager::class.java)
                val clip = ClipData.newPlainText("", nsec)
                clipboard.setPrimaryClip(clip)
                keyCopied = true
            },
        )
    }

    IconText(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        text = stringResource(id = R.string.settings_keys_private_key_hint),
        leadingIcon = Icons.Outlined.Warning,
        leadingIconSize = 16.sp,
        lineHeight = 20.sp,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        leadingIconTintColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        style = AppTheme.typography.bodySmall,
    )
}

@Preview
@Composable
fun PreviewSettingsHomeScreen() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        AccountSettingsScreen(
            state = AccountSettingsContract.UiState(
                avatarCdnImage = CdnImage("https://i.imgur.com/Z8dpmvc.png"),
                npub = "npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr",
                nsec = "nsec1w33tr4t0gg3gvrhjh5mxqzvt7xzdrrk64tr0j7mnqdfrrarfj3yqlf8hxp",
            ),
            onClose = { },
        )
    }
}
