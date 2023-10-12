package net.primal.android.messages.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.AvatarThumbnailListItemImage
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val state = viewModel.state.collectAsState()

    ChatScreen(
        state = state.value,
        onClose = onClose,
        onProfileClick = onProfileClick,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: ChatContract.UiState,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit,
    eventPublisher: (ChatContract.UiEvent) -> Unit,
) {

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            PrimalTopAppBar(
                title = "qauser",
                subtitle = "qauser@primal.net",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(CircleShape)
                    ) {
                        AvatarThumbnailListItemImage(
                            source = null,
                            modifier = Modifier.size(32.dp),
                            onClick = { onProfileClick("88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079") },
                        )
                    }
                }
            )
        },
        content = { contentPadding ->
            LazyColumn(
                modifier = Modifier.background(color = AppTheme.colorScheme.surfaceVariant),
                contentPadding = contentPadding,
            ) {
                items(count = 100) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 8.dp),
                        text = stringResource(id = R.string.chat_message_hint, it),
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier.background(color = AppTheme.colorScheme.surface),
            ) {
                PrimalDivider()
                MessageOutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp)
                        .imePadding(),
                    value = "",
                    onValueChange = { },
                )
            }
        },
    )
}

@Composable
fun MessageOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        maxLines = 10,
        enabled = true,
        placeholder = {
            Text(
                text = stringResource(
                    id = R.string.chat_message_hint,
                    "qauser"
                ),
                maxLines = 1,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodyMedium,
            )
        },
        trailingIcon = {
            AppBarIcon(
                icon = Icons.Outlined.ArrowUpward,
                onClick = {},
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        },
        textStyle = AppTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt,
            focusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt,
            focusedBorderColor = Color.Unspecified,
            unfocusedBorderColor = Color.Unspecified,
            errorBorderColor = Color.Unspecified,
            disabledBorderColor = Color.Unspecified
        ),
    )
}