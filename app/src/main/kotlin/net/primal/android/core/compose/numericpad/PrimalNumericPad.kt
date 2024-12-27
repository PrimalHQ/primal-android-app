@file:Suppress("MagicNumber")

package net.primal.android.core.compose.numericpad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.math.BigDecimal
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Subtract
import net.primal.android.core.compose.numericpad.PrimalNumericPadContract.UiEvent.NumericInputEvent
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.domain.CurrencyMode

private val PadButtonMargin = 16.dp

@Composable
fun PrimalNumericPad(
    modifier: Modifier = Modifier,
    amountInSats: String,
    onAmountInSatsChanged: (String) -> Unit,
    currencyMode: CurrencyMode,
    maximumUsdAmount: BigDecimal? = null,
) {
    val haptic = LocalHapticFeedback.current
    val viewModel = viewModel<PrimalNumericPadViewModel>()

    LaunchedEffect(amountInSats) {
        viewModel.setEvent(PrimalNumericPadContract.UiEvent.SetAmount(valueInSats = amountInSats))
    }

    LaunchedEffect(viewModel) {
        viewModel.channel.collect {
            when (it) {
                is PrimalNumericPadContract.SideEffect.AmountChanged -> onAmountInSatsChanged(it.amountInSats)
            }
        }
    }

    val onNumberClick: (Int) -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val newAmount = BigDecimal.valueOf((amountInSats + it.toString()).toDouble())
        when (currencyMode) {
            CurrencyMode.FIAT -> {
                if (newAmount <= maximumUsdAmount) {
                    val decimalPart = amountInSats.split(".")
                    val isDecimalValid = decimalPart.size != 2 || decimalPart[1].length < 2

                    if (isDecimalValid) {
                        viewModel.setEvent(NumericInputEvent.DigitInputEvent(it))
                    }
                }
            }
            CurrencyMode.SATS -> {
                viewModel.setEvent(NumericInputEvent.DigitInputEvent(it))
            }
        }
    }

    val onDotClick: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        when (currencyMode) {
            CurrencyMode.FIAT -> viewModel.setEvent(NumericInputEvent.DotInputEvent)
            CurrencyMode.SATS -> Unit
        }
    }

    val onBackspaceClick: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.setEvent(NumericInputEvent.BackspaceEvent)
    }

    val onBackspaceLongClick: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.setEvent(NumericInputEvent.ResetAmountEvent)
    }

    NumericPad(
        modifier = modifier,
        onNumberClick = onNumberClick,
        onDotClick = onDotClick,
        onBackspaceClick = onBackspaceClick,
        onBackspaceLongClick = onBackspaceLongClick,
    )
}

@Composable
private fun NumericPad(
    modifier: Modifier,
    onNumberClick: (Int) -> Unit,
    onDotClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onBackspaceLongClick: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Row {
            NumericPadNumbersRow(
                numbers = listOf(1, 2, 3),
                onClick = onNumberClick,
            )
        }

        Spacer(modifier = Modifier.height(PadButtonMargin))

        Row {
            NumericPadNumbersRow(
                numbers = listOf(4, 5, 6),
                onClick = onNumberClick,
            )
        }

        Spacer(modifier = Modifier.height(PadButtonMargin))

        Row {
            NumericPadNumbersRow(
                numbers = listOf(7, 8, 9),
                onClick = onNumberClick,
            )
        }

        Spacer(modifier = Modifier.height(PadButtonMargin))

        Row {
            NumericPadLastRow(
                onNumberClick = onNumberClick,
                onDotClick = onDotClick,
                onBackspaceClick = onBackspaceClick,
                onBackspaceLongClick = onBackspaceLongClick,
            )
        }
    }
}

@Composable
fun NumericPadNumbersRow(
    numbers: List<Int>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(numbers.size == 3)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        numbers.forEachIndexed { index, number ->
            NumericPadTextButton(
                modifier = Modifier.weight(1f),
                text = "$number",
                onClick = { onClick(number) },
            )

            if (index < 2) {
                Spacer(modifier = Modifier.width(PadButtonMargin))
            }
        }
    }
}

@Composable
fun NumericPadLastRow(
    onNumberClick: (Int) -> Unit,
    onDotClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onBackspaceLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        NumericPadTextButton(
            modifier = Modifier.weight(1f),
            text = ".",
            onClick = onDotClick,
        )

        Spacer(modifier = Modifier.width(PadButtonMargin))

        NumericPadTextButton(
            modifier = Modifier.weight(1f),
            text = "0",
            onClick = { onNumberClick(0) },
        )

        Spacer(modifier = Modifier.width(PadButtonMargin))

        NumericPadIconButton(
            modifier = Modifier.weight(1f),
            icon = PrimalIcons.Subtract,
            onClick = onBackspaceClick,
            onLongClick = onBackspaceLongClick,
        )
    }
}

@Composable
fun NumericPadButton(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    content: @Composable RowScope.() -> Unit,
) {
    PrimalFilledButton(
        modifier = modifier
            .defaultMinSize(minWidth = 88.dp, minHeight = 56.dp),
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        contentColor = AppTheme.colorScheme.onSurface,
        onClick = onClick,
        onLongClick = onLongClick,
        content = content,
    )
}

@Composable
fun NumericPadTextButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)? = null,
) {
    NumericPadButton(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Text(
            text = text,
            style = AppTheme.typography.bodyLarge,
            fontSize = 36.sp,
        )
    }
}

@Composable
fun NumericPadIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)? = null,
) {
    NumericPadButton(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
fun PreviewPrimalNumericPad() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            PrimalNumericPad(
                amountInSats = "0",
                onAmountInSatsChanged = {},
                currencyMode = CurrencyMode.SATS,
                maximumUsdAmount = BigDecimal(94000),
            )
        }
    }
}
