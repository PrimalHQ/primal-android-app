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
    errorMessageResolver: (T) -> String,
    onErrorDismiss: (() -> Unit)? = null,
    onActionPerformed: ((T) -> Unit)? = null,
) {
    LaunchedEffect(error ?: true) {
        if (error == null) return@LaunchedEffect

        val errorMessage = errorMessageResolver(error)

        val result = snackbarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Indefinite,
            withDismissAction = true,
        )

        when (result) {
            SnackbarResult.Dismissed -> onErrorDismiss?.invoke()
            SnackbarResult.ActionPerformed -> onActionPerformed?.invoke(error)
        }
    }
}
