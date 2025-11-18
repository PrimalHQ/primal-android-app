package net.primal.android.redeem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Paste
import net.primal.android.theme.AppTheme

@Composable
internal fun RedeemEnterCodeStage(
    modifier: Modifier = Modifier,
    isError: Boolean,
    isLoading: Boolean,
    promoCode: String?,
    onApplyCodeClick: (code: String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    var code by remember { mutableStateOf(promoCode ?: "") }

    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        val (content, applyButton) = createRefs()

        Column(
            modifier = Modifier.constrainAs(content) {
                top.linkTo(parent.top)
                bottom.linkTo(applyButton.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }.background(color = AppTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.redeem_code_scan_anything_subtitle),
                color = AppTheme.colorScheme.onPrimary,
                style = AppTheme.typography.bodyLarge,
            )

            PromoCodeTextField(
                modifier = Modifier.fillMaxWidth(),
                code = code,
                onCodeChange = { code = it },
                isError = isError,
            )

            TextButton(
                modifier = Modifier
                    .background(
                        color = AppTheme.colorScheme.background,
                        shape = AppTheme.shapes.extraLarge,
                    )
                    .border(
                        width = 1.dp,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                        shape = AppTheme.shapes.extraLarge,
                    ).padding(horizontal = 16.dp),
                onClick = {
                    val clipboardText = clipboardManager.getText()?.text.orEmpty().trim()
                    code = clipboardText
                },
            ) {
                Icon(
                    imageVector = PrimalIcons.Paste,
                    contentDescription = null,
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
                Text(
                    modifier = Modifier.padding(start = 7.dp),
                    text = stringResource(id = R.string.redeem_code_paste),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        }

        PrimalLoadingButton(
            text = stringResource(id = R.string.redeem_code_apply_code_button),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .constrainAs(applyButton) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            loading = isLoading,
            enabled = !isLoading && code.isNotBlank(),
            onClick = {
                keyboardController?.hide()
                onApplyCodeClick(code)
            },
        )
    }
}

@Composable
private fun PromoCodeTextField(
    modifier: Modifier = Modifier,
    code: String,
    onCodeChange: (String) -> Unit,
    isError: Boolean,
) {
    val colors = PrimalDefaults.outlinedTextFieldColors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
    )

    OutlinedTextField(
        modifier = modifier,
        value = code,
        onValueChange = onCodeChange,
        colors = colors,
        shape = AppTheme.shapes.extraLarge,
        singleLine = true,
        textStyle = AppTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        ),
        isError = isError,
        placeholder = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.redeem_code_enter_code_placeholder),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                textAlign = TextAlign.Center,
            )
        },
    )
}
