package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun OtpTextField(
    modifier: Modifier = Modifier,
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    otpCount: Int = 6,
    onCodeConfirmed: ((String) -> Unit)? = null,
    charSpacing: Dp = 8.dp,
    charWidth: Dp? = 44.dp,
    keyboardType: KeyboardType = KeyboardType.NumberPassword,
) {
    BasicTextField(
        modifier = modifier,
        value = TextFieldValue(
            text = otpText,
            selection = TextRange(otpText.length),
        ),
        onValueChange = {
            if (it.text.length <= otpCount) {
                onOtpTextChange(it.text)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (otpText.length == otpCount) ImeAction.Go else ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onGo = {
                if (otpText.length == otpCount) {
                    onCodeConfirmed?.invoke(otpText)
                }
            },
        ),
        decorationBox = {
            Row(
                modifier = Modifier.height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(charSpacing, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(otpCount) {
                    CharText(index = it, text = otpText, width = charWidth)
                }
            }
        },
    )
}

@Composable
private fun RowScope.CharText(
    index: Int,
    text: String,
    width: Dp?,
) {
    val char = when {
        index == text.length -> ""
        index > text.length -> ""
        else -> text[index].toString()
    }
    Box(
        modifier = Modifier
            .run {
                if (width == null) {
                    weight(1f)
                } else {
                    width(width)
                }
            }
            .fillMaxHeight()
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.large,
            )
            .padding(horizontal = 2.dp)
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = char,
            style = AppTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
        )
    }
}
