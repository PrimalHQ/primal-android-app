package net.primal.android.signer.provider.approvals

import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRequestsBottomSheet(viewModel: PermissionRequestsViewModel, onDismiss: () -> Unit) {
    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect {
            when (it) {
                is PermissionRequestsContract.SideEffect.RespondToIntent -> Unit
            }
        }
    }

    ModalBottomSheet(
        contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        onDismissRequest = onDismiss,
    ) {
        Text(
            modifier = Modifier.height(600.dp),
            text = "This is a PermissionsBottomSheet!",
        )
    }
}
