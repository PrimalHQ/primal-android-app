package net.primal.android.core.compose.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AdjustTemporarilySystemBarColors
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.utils.shortened
import net.primal.android.settings.zaps.PRESETS_COUNT
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZapBottomSheet(
    receiverName: String,
    defaultZapAmount: ULong,
    userZapOptions: List<ULong>? = null,
    onDismissRequest: () -> Unit,
    onZap: (ULong, String?) -> Unit,
) {
    if (userZapOptions != null && userZapOptions.size != PRESETS_COUNT) {
        throw IllegalArgumentException("There should be 6 zap options.")
    }

    val zapOptionsValues = userZapOptions ?: listOf(
        21L.toULong(),
        420.toULong(),
        1_000.toULong(),
        5_000.toULong(),
        10_000.toULong(),
        100_000.toULong(),
    )

    val zapOptions = mutableListOf(
        Pair(zapOptionsValues[0], "👍"),
        Pair(zapOptionsValues[1], "🌿"),
        Pair(zapOptionsValues[2], "🤙"),
        Pair(zapOptionsValues[3], "💜"),
        Pair(zapOptionsValues[4], "🔥"),
        Pair(zapOptionsValues[5], "🚀"),
    )

    var selectedZapAmount by remember { mutableStateOf(defaultZapAmount) }
    var selectedZapComment by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    AdjustTemporarilySystemBarColors(
        navigationBarColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    )
    ModalBottomSheet(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        tonalElevation = 0.dp,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        ) {
            ZapTitle(receiverName = receiverName, amount = selectedZapAmount)
            ZapOptions(
                zapOptions = zapOptions,
                selectedZapAmount = selectedZapAmount,
                onSelectedZapAmountChange = { amount ->
                    selectedZapAmount = amount
                    selectedZapComment =
                        zapOptions.find { it.first == amount }?.second ?: selectedZapComment
                },
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .requiredHeight(height = 54.dp),
                singleLine = true,
                colors = PrimalDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(8.dp),
                value = selectedZapComment,
                onValueChange = { selectedZapComment = it },
                textStyle = AppTheme.typography.bodySmall,

                placeholder = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.zap_bottom_sheet_comment_placeholder),
                        textAlign = TextAlign.Left,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    )
                },
            )
            Spacer(modifier = Modifier.height(24.dp))
            PrimalLoadingButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp),
                text = stringResource(id = R.string.zap_bottom_sheet_zap_button),
                leadingIcon = ImageVector.vectorResource(id = R.drawable.zap),
                onClick = {
                    onDismissRequest()
                    onZap(selectedZapAmount, selectedZapComment)
                },
            )
        }
    }
}

@Composable
private fun ZapOptions(
    zapOptions: List<Pair<ULong, String>>,
    selectedZapAmount: ULong,
    onSelectedZapAmountChange: (ULong) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(12.dp),
    ) {
        items(zapOptions) { (defaultAmount, defaultComment) ->
            ZapOption(
                defaultAmount = defaultAmount,
                defaultComment = defaultComment,
                selected = selectedZapAmount == defaultAmount,
                onClick = {
                    onSelectedZapAmountChange(defaultAmount)
                },
            )
        }
    }
}

@Composable
private fun ZapOption(
    defaultAmount: ULong,
    defaultComment: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val selectedBorderGradientColors = Brush.linearGradient(
        listOf(
            AppTheme.colorScheme.primary,
            AppTheme.colorScheme.primary,
        ),
    )

    val backgroundColor =
        if (selected) AppTheme.colorScheme.surface else AppTheme.extraColorScheme.surfaceVariantAlt1
    val borderWidth = if (selected) 1.dp else 0.dp
    val borderBrush = if (selected) {
        selectedBorderGradientColors
    } else {
        Brush.linearGradient(
            listOf(
                Color.Transparent,
                Color.Transparent,
            ),
        )
    }

    Box(
        modifier = Modifier
            .padding(all = 12.dp)
            .clip(AppTheme.shapes.small)
            .border(
                width = borderWidth,
                shape = AppTheme.shapes.small,
                brush = borderBrush,
            )
            .background(
                color = backgroundColor,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .requiredHeight(88.dp)
            .requiredWidth(88.dp)
            .aspectRatio(1f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = defaultComment,
                fontWeight = FontWeight.W600,
                fontSize = TextUnit(
                    value = 20f,
                    type = TextUnitType.Sp,
                ),
            )
            Text(text = defaultAmount.shortened())
        }
    }
}

@Composable
private fun ZapTitle(receiverName: String, amount: ULong) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        Text(
            buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        fontWeight = FontWeight.W700,
                        fontSize = TextUnit(
                            value = 20f,
                            type = TextUnitType.Sp,
                        ),
                    ),
                ) {
                    append("ZAP ${receiverName.uppercase()} ")
                }
                withStyle(
                    style = SpanStyle(
                        color = AppTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.W900,
                        fontSize = TextUnit(
                            value = 20f,
                            type = TextUnitType.Sp,
                        ),
                    ),
                ) {
                    append("${amount.shortened()} ")
                }
                withStyle(
                    style = SpanStyle(
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        fontWeight = FontWeight.W700,
                        fontSize = TextUnit(
                            value = 14f,
                            type = TextUnitType.Sp,
                        ),
                    ),
                ) {
                    append("SATS")
                }
            },
        )
    }
}
