package net.primal.android.signer.provider.connect

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import net.primal.android.theme.AppTheme
import net.primal.domain.account.model.LocalSignerMethodResponse
import net.primal.domain.account.model.LocalSignerMethodResponse.Success.GetPublicKey
import net.primal.domain.account.model.TrustLevel
import net.primal.domain.nostr.cryptography.utils.assureValidNpub

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun AndroidConnectScreen(
    viewModel: AndroidConnectViewModel,
    onDismiss: () -> Unit,
    onConnectionApproved: (LocalSignerMethodResponse) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()
    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect {
            when (it) {
                is AndroidConnectContract.SideEffect.ConnectionSuccess -> {
                    onConnectionApproved(
                        GetPublicKey(
                            eventId = Uuid.random().toString(),
                            pubkey = it.userId.assureValidNpub(),
                        ),
                    )
                }

                is AndroidConnectContract.SideEffect.ConnectionFailure -> {
                    onConnectionApproved(
                        LocalSignerMethodResponse.Error(
                            eventId = Uuid.random().toString(),
                            message = it.error.message ?: "Unable to sign in.",
                        ),
                    )
                }
            }
        }
    }

    AndroidConnectScreen(
        state = uiState.value,
        eventPublisher = viewModel::setEvent,
        onDismiss = onDismiss,
    )
}

@ExperimentalMaterial3Api
@Composable
private fun AndroidConnectScreen(
    state: AndroidConnectContract.UiState,
    eventPublisher: (AndroidConnectContract.UiEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        onDismissRequest = onDismiss,
    ) {
        Text(
            modifier = Modifier.height(600.dp),
            text = "This is SignerConnectBottomSheet!",
        )

        Button(
            onClick = {
                eventPublisher(
                    AndroidConnectContract.UiEvent.ConnectUser(
                        userId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
                        trustLevel = TrustLevel.Full,
                    ),
                )
            },
        ) {
            Text("Connect QA with ${state.appPackageName}")
        }
    }
}

data class AppDisplayInfo(
    val name: String,
    val icon: Drawable,
)
