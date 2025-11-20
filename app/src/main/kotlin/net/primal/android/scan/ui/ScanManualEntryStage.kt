package net.primal.android.scan.ui

import PasteAlt
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.CheckCircleOutline
import net.primal.android.scan.utils.isValidPromoCode
import net.primal.android.scanner.domain.QrCodeDataType
import net.primal.android.theme.AppTheme

private val SUCCESS_COLOR = Color(0xFF52CE0A)

@Composable
internal fun ScanManualEntryStage(
    modifier: Modifier = Modifier,
    isError: Boolean,
    isLoading: Boolean,
    value: String?,
    onValueChanged: () -> Unit,
    onApplyClick: (code: String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var text by remember { mutableStateOf(value ?: "") }

    val isValidInput = remember(text) { text.trim().isValidScanInput() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.scan_anything_subtitle),
                color = AppTheme.colorScheme.onPrimary,
                style = AppTheme.typography.bodyLarge,
            )

            ParsingInputTextField(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                onValueChange = {
                    text = it
                    onValueChanged()
                },
                isError = isError,
                isValidInput = isValidInput,
            )

            PasteCodeButton(
                modifier = Modifier
                    .background(
                        color = AppTheme.colorScheme.background,
                        shape = AppTheme.shapes.extraLarge,
                    )
                    .border(
                        width = 1.dp,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                        shape = AppTheme.shapes.extraLarge,
                    )
                    .padding(horizontal = 16.dp),
                onPaste = { pastedText ->
                    text = pastedText
                    onValueChanged()
                },
            )
        }

        PrimalLoadingButton(
            text = stringResource(id = R.string.scan_code_apply_button),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            loading = isLoading,
            enabled = !isLoading && text.isNotBlank(),
            onClick = {
                keyboardController?.hide()
                onApplyClick(text)
            },
        )
    }
}

@Composable
private fun PasteCodeButton(modifier: Modifier, onPaste: (String) -> Unit) {
    val context = LocalContext.current
    val clipboardManager = remember(context) {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    TextButton(
        modifier = modifier.height(40.dp),
        onClick = {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val clipText = clipData.getItemAt(0).coerceToText(context).toString()
                onPaste(clipText.trim())
            }
        },
    ) {
        Icon(
            imageVector = PrimalIcons.PasteAlt,
            contentDescription = null,
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
        Text(
            modifier = Modifier.padding(start = 11.dp, top = 3.dp),
            text = stringResource(id = R.string.scan_code_paste),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
    }
}

@Composable
private fun ParsingInputTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    isValidInput: Boolean,
) {
    val borderColor = when {
        isError -> AppTheme.colorScheme.error
        isValidInput -> SUCCESS_COLOR
        else -> Color.Transparent
    }

    val colors = PrimalDefaults.outlinedTextFieldColors(
        focusedBorderColor = borderColor,
        unfocusedBorderColor = borderColor,
    )

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        colors = colors,
        shape = AppTheme.shapes.extraLarge,
        singleLine = true,
        textStyle = AppTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        ),
        isError = isError,
        placeholder = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.scan_code_enter_placeholder),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                textAlign = TextAlign.Center,
            )
        },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(22.dp),
                imageVector = PrimalIcons.CheckCircleOutline,
                contentDescription = null,
                tint = Color.Transparent,
            )
        },
        trailingIcon = {
            Icon(
                modifier = Modifier.size(22.dp),
                imageVector = PrimalIcons.CheckCircleOutline,
                contentDescription = null,
                tint = if (isValidInput && !isError) SUCCESS_COLOR else Color.Transparent,
            )
        },
    )
}

private fun String.isValidScanInput(): Boolean {
    return QrCodeDataType.from(this) != null || isValidPromoCode()
}
