package net.primal.android.redeem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.auth.compose.OnboardingButton
import net.primal.android.core.compose.OtpTextField
import net.primal.android.theme.AppTheme

private val INCORRECT_COLOR = Color(0xFFE20505)
private const val PROMO_CODE_LENGTH = 8

@Composable
internal fun RedeemEnterCodeStage(
    modifier: Modifier = Modifier,
    isError: Boolean,
    isLoading: Boolean,
    promoCode: String?,
    onApplyCodeClick: (code: String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var promoCode by remember { mutableStateOf(promoCode ?: "") }
    var isDirty by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .imePadding()
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.redeem_code_enter_code_description),
                color = Color.White,
                style = AppTheme.typography.bodyMedium,
            )

            OtpTextField(
                charWidth = null,
                keyboardType = KeyboardType.Text,
                otpText = promoCode,
                onOtpTextChange = {
                    isDirty = true
                    promoCode = it.uppercase()
                },
                onCodeConfirmed = {
                    isDirty = false
                    keyboardController?.hide()
                    onApplyCodeClick(it)
                },
                backgroundColor = Color.White,
                otpCount = PROMO_CODE_LENGTH,
                alpha = 0.8f,
            )
            if (isError && !isDirty) {
                IncorrectCodeBadge()
            }
        }

        OnboardingButton(
            text = stringResource(id = R.string.redeem_code_apply_code_button),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(alignment = Alignment.CenterHorizontally),
            loading = isLoading,
            enabled = !isLoading && (!isError || isDirty) && promoCode.length == 8,
            onClick = {
                isDirty = false
                keyboardController?.hide()
                onApplyCodeClick(promoCode)
            },
        )
    }
}

@Composable
private fun IncorrectCodeBadge(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .clip(AppTheme.shapes.extraLarge)
            .background(INCORRECT_COLOR)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        text = stringResource(id = R.string.redeem_code_incorrect_code_message),
        color = Color.White,
        style = AppTheme.typography.bodySmall,
    )
}
