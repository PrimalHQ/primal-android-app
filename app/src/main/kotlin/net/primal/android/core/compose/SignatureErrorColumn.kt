package net.primal.android.core.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.errors.SignatureUiError
import net.primal.android.core.errors.SignatureUiError.SigningKeyNotFound
import net.primal.android.core.errors.SignatureUiError.SigningRejected

/**
 * Displays appropriate error message if [signatureUiError] is not null.  Otherwise shows [content].
 */
@Composable
fun SignatureErrorColumn(
    signatureUiError: SignatureUiError?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
    content: @Composable () -> Unit,
) {
    when (signatureUiError) {
        is SigningKeyNotFound -> {
            ListNoContent(
                modifier = modifier,
                contentPadding = contentPadding,
                noContentText = stringResource(id = R.string.app_npub_login_error),
                refreshButtonVisible = false,
            )
        }

        is SigningRejected -> {
            ListNoContent(
                modifier = modifier,
                contentPadding = contentPadding,
                noContentText = stringResource(id = R.string.app_error_sign_unauthorized),
                refreshButtonVisible = false,
            )
        }

        null -> content
    }
}
