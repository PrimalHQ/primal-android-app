package net.primal.android.core.compose

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun <T> SnackbarErrorHandler(
    error: T?,
    snackbarHostState: SnackbarHostState,
    errorMessageResolver: @Composable (T) -> String,
    actionLabel: String? = null,
    onErrorDismiss: (() -> Unit)? = null,
    onActionPerformed: ((T) -> Unit)? = null,
) {
    val errorMessage = if (error != null) errorMessageResolver(error) else null

    LaunchedEffect(error ?: true) {
        if (error == null || errorMessage == null) return@LaunchedEffect

        val result = snackbarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Indefinite,
            withDismissAction = true,
            actionLabel = actionLabel,
        )

        when (result) {
            SnackbarResult.Dismissed -> onErrorDismiss?.invoke()
            SnackbarResult.ActionPerformed -> onActionPerformed?.invoke(error)
        }
    }
}
