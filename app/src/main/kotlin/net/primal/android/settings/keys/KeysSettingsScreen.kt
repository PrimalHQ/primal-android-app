package net.primal.android.settings.keys

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import net.primal.android.R
import net.primal.android.core.compose.BiometricPrompt
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Key
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.links.CdnImage

@Composable
fun KeysSettingsScreen(viewModel: KeysSettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    KeysSettingsScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeysSettingsScreen(state: KeysSettingsContract.UiState, onClose: () -> Unit) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_keys_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
            ) {
                item {
                    PublicKeySection(
                        npub = state.npub,
                        avatarCdnImage = state.avatarCdnImage,
                        legendaryCustomization = state.legendaryCustomization,
                    )
                }

                item {
                    PrimalDivider(modifier = Modifier.padding(vertical = 16.dp))
                }

                item {
                    state.nsec?.let {
                        PrivateKeySection(nsec = state.nsec)
                    }
                }
            }
        },
    )
}

@Composable
fun PublicKeySection(
    npub: String,
    avatarCdnImage: CdnImage?,
    legendaryCustomization: LegendaryCustomization?,
) {
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
            .padding(top = 8.dp, bottom = 16.dp)
            .height(72.dp)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.medium,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.padding(start = 16.dp)) {
            UniversalAvatarThumbnail(
                avatarCdnImage = avatarCdnImage,
                legendaryCustomization = legendaryCustomization,
            )
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
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
    var privateKeyVisible by rememberSaveable { mutableStateOf(false) }
    var authenticated by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(authenticated) {
        if (authenticated) {
            delay(1.minutes)
            authenticated = false
        }
    }

    PrivateKeyTextTitle(
        privateKeyVisible = privateKeyVisible,
        onKeyVisibilityChanged = { privateKeyVisible = it },
        authenticated = authenticated,
        onAuthenticated = { authenticated = true },
    )

    PrivateKeyTextValue(
        nsec = nsec,
        privateKeyVisible = privateKeyVisible,
    )

    PrivateKeyCopyButton(
        nsec = nsec,
        authenticated = authenticated,
        onAuthenticated = { authenticated = true },
    )

    IconText(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        text = stringResource(id = R.string.settings_keys_private_key_hint),
        leadingIcon = Icons.Outlined.Warning,
        iconSize = 16.sp,
        lineHeight = 20.sp,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        leadingIconTintColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        style = AppTheme.typography.bodySmall,
    )
}

@Composable
private fun PrivateKeyCopyButton(
    nsec: String,
    authenticated: Boolean,
    onAuthenticated: () -> Unit,
) {
    val context = LocalContext.current
    Box(modifier = Modifier.padding(vertical = 8.dp)) {
        var keyCopied by remember { mutableStateOf(false) }
        var showCopyBiometricPrompt by rememberSaveable { mutableStateOf(false) }
        if (showCopyBiometricPrompt) {
            BiometricPrompt(
                onAuthSuccess = {
                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                    val clip = ClipData.newPlainText("", nsec)
                    clipboard.setPrimaryClip(clip)
                    keyCopied = true
                    onAuthenticated()
                    showCopyBiometricPrompt = false
                },
                onAuthDismiss = {
                    showCopyBiometricPrompt = false
                },
            )
        }
        PrimalLoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            leadingIcon = if (keyCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
            text = if (keyCopied) {
                stringResource(id = R.string.settings_keys_key_copied)
            } else {
                stringResource(id = R.string.settings_keys_copy_private_key)
            },
            onClick = {
                if (authenticated) {
                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                    val clip = ClipData.newPlainText("", nsec)
                    clipboard.setPrimaryClip(clip)
                    keyCopied = true
                } else {
                    showCopyBiometricPrompt = true
                }
            },
        )
    }
}

@Composable
private fun PrivateKeyTextValue(privateKeyVisible: Boolean, nsec: String) {
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
}

@Composable
private fun PrivateKeyTextTitle(
    privateKeyVisible: Boolean,
    onKeyVisibilityChanged: (Boolean) -> Unit,
    authenticated: Boolean,
    onAuthenticated: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var showPrivateKeyBiometricPrompt by rememberSaveable { mutableStateOf(false) }
        if (showPrivateKeyBiometricPrompt) {
            BiometricPrompt(
                onAuthSuccess = {
                    onKeyVisibilityChanged(true)
                    onAuthenticated()
                    showPrivateKeyBiometricPrompt = false
                },
                onAuthDismiss = {
                    showPrivateKeyBiometricPrompt = false
                },
            )
        }

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
                    if (!privateKeyVisible) {
                        if (authenticated) {
                            onKeyVisibilityChanged(true)
                        } else {
                            showPrivateKeyBiometricPrompt = true
                        }
                    } else {
                        onKeyVisibilityChanged(false)
                    }
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
}

@Preview
@Composable
fun PreviewSettingsHomeScreen() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        KeysSettingsScreen(
            state = KeysSettingsContract.UiState(
                avatarCdnImage = CdnImage("https://i.imgur.com/Z8dpmvc.png"),
                npub = "npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr",
                nsec = "nsec1w33tr4t0gg3gvrhjh5mxqzvt7xzdrrk64tr0j7mnqdfrrarfj3yqlf8hxp",
            ),
            onClose = { },
        )
    }
}

@Preview
@Composable
fun PreviewSettingsHomeScreenNpubLogin() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        KeysSettingsScreen(
            state = KeysSettingsContract.UiState(
                avatarCdnImage = CdnImage("https://i.imgur.com/Z8dpmvc.png"),
                npub = "npub16c0nh3dnadzqpm76uctf5hqhe2lny344zsmpm6feee9p5rdxaa9q586nvr",
                nsec = null,
            ),
            onClose = { },
        )
    }
}
