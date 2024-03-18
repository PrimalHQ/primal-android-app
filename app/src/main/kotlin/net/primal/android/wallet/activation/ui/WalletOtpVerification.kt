package net.primal.android.wallet.activation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.OtpTextField
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletPrimalActivation
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun WalletOtpVerification(
    modifier: Modifier,
    email: String,
    code: String,
    onCodeChanged: (String) -> Unit,
    onCodeConfirmed: () -> Unit,
    errorContent: @Composable ColumnScope.() -> Unit,
    otpTheme: PrimalTheme = LocalPrimalTheme.current,
) {
    val isKeyboardVisible by keyboardVisibilityAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = !isKeyboardVisible,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = ExitTransition.None,
        ) {
            Image(
                modifier = Modifier.padding(vertical = 16.dp),
                imageVector = PrimalIcons.WalletPrimalActivation,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = AppTheme.colorScheme.onSurface),
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .padding(top = 32.dp),
            text = stringResource(id = R.string.wallet_activation_pending_code_subtitle),
            textAlign = TextAlign.Center,
            color = AppTheme.colorScheme.onSurface,
            style = AppTheme.typography.titleLarge,
        )

        val textHint = stringResource(id = R.string.wallet_activation_pending_code_hint, email)
        val textHintAnnotation = buildAnnotatedString {
            append(textHint)
            val startIndex = textHint.indexOf(email)
            if (startIndex >= 0) {
                val endIndex = startIndex + email.length
                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                    start = startIndex,
                    end = endIndex,
                )
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .padding(top = 16.dp, bottom = 32.dp),
            text = textHintAnnotation,
            textAlign = TextAlign.Center,
            color = AppTheme.colorScheme.onSurface,
            style = AppTheme.typography.bodyMedium,
        )

        PrimalTheme(primalTheme = otpTheme) {
            OtpTextField(
                modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                otpText = code,
                onOtpTextChange = {
                    if (it.isDigitsOnly()) {
                        onCodeChanged(it.trim())
                    }
                },
                onCodeConfirmed = {
                    if (code.isOtpCodeValid()) {
                        keyboardController?.hide()
                        onCodeConfirmed()
                    }
                },
            )
        }

        errorContent()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private const val CODE_LENGTH = 6

fun String.isOtpCodeValid() = this.isDigitsOnly() && this.length == CODE_LENGTH
