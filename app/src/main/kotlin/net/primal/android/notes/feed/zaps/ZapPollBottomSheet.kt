package net.primal.android.notes.feed.zaps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.delay
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.zaps.ZAP_ACTION_DELAY
import net.primal.android.core.utils.shortened
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.core.utils.CurrencyConversionUtils.fromSatsToUsd
import net.primal.core.utils.CurrencyConversionUtils.toBigDecimal
import net.primal.core.utils.generateAmountChips

private const val ZAP_POLL_CHIP_COLUMNS = 3
private const val MAX_CHIPS = 6
private const val SHORTEN_AMOUNT_THRESHOLD = 100_000L
private const val SMALL_RANGE_THRESHOLD = 6
private const val TWO_BY_TWO_GRID_SIZE = 4
private const val CUSTOM_AMOUNT_MAX_DIGITS = 8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZapPollBottomSheet(
    valueMinimum: Long?,
    valueMaximum: Long?,
    exchangeRate: Double,
    defaultZapAmounts: List<Long> = emptyList(),
    onDismissRequest: () -> Unit,
    onVote: (amount: Long, comment: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        tonalElevation = 0.dp,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        ZapPollBottomSheetContent(
            valueMinimum = valueMinimum,
            valueMaximum = valueMaximum,
            exchangeRate = exchangeRate,
            defaultZapAmounts = defaultZapAmounts,
            onDismissRequest = onDismissRequest,
            onVote = onVote,
        )
    }
}

@Composable
private fun ZapPollBottomSheetContent(
    valueMinimum: Long?,
    valueMaximum: Long?,
    exchangeRate: Double,
    defaultZapAmounts: List<Long> = emptyList(),
    onDismissRequest: () -> Unit,
    onVote: (amount: Long, comment: String?) -> Unit,
) {
    val hasConstraints = valueMinimum != null || valueMaximum != null
    val isFixedAmount = hasConstraints && valueMinimum == valueMaximum
    val isSmallRange = hasConstraints && valueMinimum != null && valueMaximum != null &&
        (valueMaximum - valueMinimum + 1) <= SMALL_RANGE_THRESHOLD

    val chips = remember(valueMinimum, valueMaximum, defaultZapAmounts) {
        if (hasConstraints && valueMinimum != null && valueMaximum != null) {
            generateAmountChips(valueMinimum, valueMaximum)
        } else {
            defaultZapAmounts.sorted().take(MAX_CHIPS)
        }
    }

    var selectedAmount by remember { mutableLongStateOf(chips.firstOrNull() ?: 1) }
    var selectedChipIndex by remember { mutableIntStateOf(0) }
    var customAmountText by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardVisible by keyboardVisibilityAsState()

    var isVoteCooldownActive by remember { mutableStateOf(false) }
    LaunchedEffect(isVoteCooldownActive) {
        if (isVoteCooldownActive) {
            delay(ZAP_ACTION_DELAY)
            isVoteCooldownActive = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        ZapPollHeader(amount = selectedAmount, exchangeRate = exchangeRate)

        Spacer(modifier = Modifier.height(35.dp))

        ZapPollAmountSection(
            hasConstraints = hasConstraints,
            isFixedAmount = isFixedAmount,
            isSmallRange = isSmallRange,
            chips = chips,
            selectedChipIndex = selectedChipIndex,
            customAmountText = customAmountText,
            valueMinimum = valueMinimum,
            valueMaximum = valueMaximum,
            onChipSelected = { amount, index ->
                keyboardController?.hide()
                selectedAmount = amount
                customAmountText = amount.toString()
                selectedChipIndex = index
            },
            onCustomAmountChange = { text, amount ->
                customAmountText = text
                if (amount != null) selectedAmount = amount
                selectedChipIndex = -1
            },
        )

        Spacer(modifier = Modifier.height(24.dp))

        ZapPollCommentField(value = comment, onValueChange = { comment = it })

        Spacer(modifier = Modifier.height(40.dp))

        ZapPollVoteButton(
            isVisible = !keyboardVisible,
            isEnabled = selectedAmount > 0 && !isVoteCooldownActive,
            onVote = {
                isVoteCooldownActive = true
                onDismissRequest()
                onVote(selectedAmount, comment.ifBlank { null })
            },
        )
    }
}

@Composable
private fun ZapPollAmountSection(
    hasConstraints: Boolean,
    isFixedAmount: Boolean,
    isSmallRange: Boolean,
    chips: List<Long>,
    selectedChipIndex: Int,
    customAmountText: String,
    valueMinimum: Long?,
    valueMaximum: Long?,
    onChipSelected: (amount: Long, index: Int) -> Unit,
    onCustomAmountChange: (text: String, amount: Long?) -> Unit,
) {
    if (!isFixedAmount && chips.isNotEmpty()) {
        ZapPollAmountChips(
            chips = chips,
            selectedIndex = selectedChipIndex,
            onChipSelected = onChipSelected,
        )
        Spacer(modifier = Modifier.height(30.dp))
    }

    ZapPollHintText(
        hasConstraints = hasConstraints,
        isFixedAmount = isFixedAmount,
        isSmallRange = isSmallRange,
        valueMinimum = valueMinimum,
        valueMaximum = valueMaximum,
    )

    if (!isFixedAmount && !isSmallRange) {
        Spacer(modifier = Modifier.height(12.dp))
        ZapPollCustomAmountInput(
            hasConstraints = hasConstraints,
            customAmountText = customAmountText,
            valueMinimum = valueMinimum,
            valueMaximum = valueMaximum,
            onCustomAmountChange = onCustomAmountChange,
        )
    }
}

@Composable
private fun ZapPollCustomAmountInput(
    hasConstraints: Boolean,
    customAmountText: String,
    valueMinimum: Long?,
    valueMaximum: Long?,
    onCustomAmountChange: (text: String, amount: Long?) -> Unit,
) {
    val hasError = if (hasConstraints && valueMinimum != null && valueMaximum != null) {
        customAmountText.isNotEmpty() &&
            (customAmountText.toLongOrNull()?.let { it !in valueMinimum..valueMaximum } ?: true)
    } else {
        customAmountText.isNotEmpty() && (customAmountText.toLongOrNull() ?: 0) <= 0
    }

    ZapPollCustomAmountField(
        value = customAmountText,
        isError = hasError,
        onValueChange = { newValue ->
            when {
                newValue.isEmpty() -> onCustomAmountChange("", null)
                newValue.isDigitsOnly() && newValue.length <= CUSTOM_AMOUNT_MAX_DIGITS -> {
                    val parsed = newValue.toLongOrNull() ?: 0
                    if (parsed > 0) onCustomAmountChange(newValue, parsed)
                }
            }
        },
    )
}

@Composable
private fun ZapPollVoteButton(
    isVisible: Boolean,
    isEnabled: Boolean,
    onVote: () -> Unit,
) {
    if (isVisible) {
        PrimalLoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 24.dp),
            enabled = isEnabled,
            text = stringResource(id = R.string.zap_poll_vote_button),
            onClick = { if (isEnabled) onVote() },
        )
    }
}

@Composable
private fun ZapPollHeader(amount: Long, exchangeRate: Double) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 35.dp),
    ) {
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.zap_poll_header_zap) + " ")
                withStyle(
                    SpanStyle(
                        color = AppTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                    ),
                ) {
                    append("%,d".format(amount) + " ")
                }
                append(stringResource(R.string.zap_poll_header_sats))
            },
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )

        if (exchangeRate > 0) {
            val usdAmount = amount.toString().toBigDecimal().fromSatsToUsd(exchangeRate).toPlainString()
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = "$$usdAmount USD",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ZapPollAmountChips(
    chips: List<Long>,
    selectedIndex: Int,
    onChipSelected: (amount: Long, index: Int) -> Unit,
) {
    val rows = remember(chips) {
        if (chips.size == TWO_BY_TWO_GRID_SIZE) chips.chunked(2) else chips.chunked(ZAP_POLL_CHIP_COLUMNS)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { rowChips ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowChips.forEach { amount ->
                    val index = chips.indexOf(amount)
                    ZapPollAmountChip(
                        modifier = Modifier.weight(1f),
                        amount = amount,
                        selected = index == selectedIndex,
                        onClick = { onChipSelected(amount, index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ZapPollAmountChip(
    modifier: Modifier = Modifier,
    amount: Long,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (selected) {
        AppTheme.colorScheme.surface
    } else {
        AppTheme.extraColorScheme.surfaceVariantAlt2
    }
    val borderWidth = if (selected) 1.dp else 0.dp
    val borderColor = if (selected) AppTheme.colorScheme.tertiary else AppTheme.colorScheme.outline

    Box(
        modifier = modifier
            .clip(AppTheme.shapes.extraLarge)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = AppTheme.shapes.extraLarge,
            )
            .background(color = backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (amount >= SHORTEN_AMOUNT_THRESHOLD) amount.shortened() else "%,d".format(amount),
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colorScheme.onSurface,
            )
            Text(
                text = " sats",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }
    }
}

@Composable
private fun ZapPollHintText(
    hasConstraints: Boolean,
    isFixedAmount: Boolean,
    isSmallRange: Boolean,
    valueMinimum: Long?,
    valueMaximum: Long?,
) {
    val text = when {
        !hasConstraints -> stringResource(R.string.zap_poll_unconstrained_hint)
        isFixedAmount && valueMinimum != null -> stringResource(
            R.string.zap_poll_fixed_amount,
            "%,d".format(valueMinimum),
        )
        isSmallRange && valueMinimum != null && valueMaximum != null -> stringResource(
            R.string.zap_poll_range_hint,
            "%,d".format(valueMinimum),
            "%,d".format(valueMaximum),
        )
        valueMinimum != null && valueMaximum != null -> stringResource(
            R.string.zap_poll_custom_amount_hint,
            "%,d".format(valueMinimum),
            "%,d".format(valueMaximum),
        )
        else -> stringResource(R.string.zap_poll_unconstrained_hint)
    }

    Text(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        text = text,
        style = AppTheme.typography.bodySmall,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ZapPollCustomAmountField(
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
) {
    val errorBorderColor = AppTheme.colorScheme.error.copy(alpha = 0.2f)

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 24.dp),
        singleLine = true,
        isError = isError,
        colors = PrimalDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            errorBorderColor = errorBorderColor,
        ),
        shape = AppTheme.shapes.extraLarge,
        value = value,
        onValueChange = onValueChange,
        textStyle = AppTheme.typography.bodyMedium,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
        placeholder = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.zap_poll_enter_custom),
                textAlign = TextAlign.Left,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            )
        },
    )
}

@Composable
private fun ZapPollCommentField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 24.dp),
        singleLine = true,
        colors = PrimalDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
        ),
        shape = AppTheme.shapes.extraLarge,
        value = value,
        onValueChange = onValueChange,
        textStyle = AppTheme.typography.bodyMedium,
        placeholder = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.zap_poll_add_comment),
                textAlign = TextAlign.Left,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            )
        },
    )
}

// region Previews

private val previewDefaultZapAmounts = listOf(21L, 420L, 1_000L, 5_000L, 10_000L, 100_000L)

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewZapPollBottomSheetFixedAmount() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            ZapPollBottomSheet(
                valueMinimum = 21,
                valueMaximum = 21,
                exchangeRate = 43_000.0,
                onDismissRequest = {},
                onVote = { _, _ -> },
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewZapPollBottomSheetTwoItems() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            ZapPollBottomSheet(
                valueMinimum = 21,
                valueMaximum = 22,
                exchangeRate = 43_000.0,
                onDismissRequest = {},
                onVote = { _, _ -> },
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewZapPollBottomSheetThreeItems() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            ZapPollBottomSheet(
                valueMinimum = 21,
                valueMaximum = 23,
                exchangeRate = 43_000.0,
                onDismissRequest = {},
                onVote = { _, _ -> },
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewZapPollBottomSheetFourItems() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            ZapPollBottomSheet(
                valueMinimum = 21,
                valueMaximum = 24,
                exchangeRate = 43_000.0,
                onDismissRequest = {},
                onVote = { _, _ -> },
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewZapPollBottomSheetFiveItems() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            ZapPollBottomSheet(
                valueMinimum = 21,
                valueMaximum = 25,
                exchangeRate = 43_000.0,
                onDismissRequest = {},
                onVote = { _, _ -> },
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewZapPollBottomSheetSixItems() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            ZapPollBottomSheet(
                valueMinimum = 21,
                valueMaximum = 26,
                exchangeRate = 43_000.0,
                onDismissRequest = {},
                onVote = { _, _ -> },
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewZapPollBottomSheetLargeRange() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            ZapPollBottomSheet(
                valueMinimum = 21,
                valueMaximum = 21_000,
                exchangeRate = 43_000.0,
                onDismissRequest = {},
                onVote = { _, _ -> },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewZapPollBottomSheetUnconstrained() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            ZapPollBottomSheet(
                valueMinimum = null,
                valueMaximum = null,
                exchangeRate = 43_000.0,
                defaultZapAmounts = previewDefaultZapAmounts,
                onDismissRequest = {},
                onVote = { _, _ -> },
            )
        }
    }
}

// endregion
