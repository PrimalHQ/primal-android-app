package net.primal.android.core.compose.feed.zaps

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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.utils.shortened
import net.primal.android.nostr.model.primal.content.ContentZapConfigItem
import net.primal.android.nostr.model.primal.content.DEFAULT_ZAP_CONFIG
import net.primal.android.settings.zaps.PRESETS_COUNT
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZapBottomSheet(
    receiverName: String,
    zappingState: ZappingState,
    onDismissRequest: () -> Unit,
    onZap: (Long, String?) -> Unit,
) {
    val zapConfig: List<ContentZapConfigItem> = zappingState.ensureZapConfig()

    var customZapAmount by remember { mutableStateOf("") }
    var selectedZapComment by remember { mutableStateOf(zapConfig.first().message) }
    var selectedZapAmount by remember { mutableLongStateOf(zapConfig.first().amount) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardVisible by keyboardVisibilityAsState()

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
            ZapTitle(
                modifier = Modifier.padding(horizontal = 16.dp),
                receiverName = receiverName,
                amount = selectedZapAmount,
            )
            ZapOptions(
                zapConfig = zapConfig,
                selectedZapAmount = selectedZapAmount,
                onSelectedZapAmountChange = { amount ->
                    keyboardController?.hide()
                    selectedZapAmount = amount
                    selectedZapComment = zapConfig.find {
                        it.amount == amount
                    }?.message ?: selectedZapComment
                },
            )
            ZapCustomAmountOutlinedTextField(
                value = customZapAmount,
                onValueChange = {
                    when {
                        it.isEmpty() -> customZapAmount = ""
                        it.isDigitsOnly() && it.length <= 8 && it.toLong() > 0 -> customZapAmount = it
                    }
                    selectedZapAmount = customZapAmount.toLongOrNull() ?: zapConfig.first().amount
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            ZapCommentOutlinedTextField(
                value = selectedZapComment,
                onValueChange = { selectedZapComment = it },
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (!keyboardVisible) {
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
}

@Composable
private fun ZapCommentOutlinedTextField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        singleLine = true,
        colors = PrimalDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(8.dp),
        value = value,
        onValueChange = onValueChange,
        textStyle = AppTheme.typography.bodyMedium,
        placeholder = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.zap_bottom_sheet_comment_placeholder),
                textAlign = TextAlign.Left,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            )
        },
    )
}

@Composable
private fun ZapCustomAmountOutlinedTextField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        singleLine = true,
        colors = PrimalDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(8.dp),
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
                text = stringResource(id = R.string.zap_bottom_sheet_custom_amount_placeholder),
                textAlign = TextAlign.Left,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            )
        },
    )
}

private const val ZAP_OPTIONS_COLUMNS_COUNT = 3

@Composable
private fun ZapOptions(
    zapConfig: List<ContentZapConfigItem>,
    selectedZapAmount: Long,
    onSelectedZapAmountChange: (Long) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(ZAP_OPTIONS_COLUMNS_COUNT),
        contentPadding = PaddingValues(12.dp),
    ) {
        items(zapConfig) {
            ZapOption(
                defaultAmount = it.amount,
                defaultEmoji = it.emoji,
                selected = selectedZapAmount == it.amount,
                onClick = {
                    onSelectedZapAmountChange(it.amount)
                },
            )
        }
    }
}

@Composable
private fun ZapOption(
    defaultAmount: Long,
    defaultEmoji: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (selected) AppTheme.colorScheme.surface else AppTheme.extraColorScheme.surfaceVariantAlt1
    val borderWidth = if (selected) 1.dp else 0.dp
    val borderColor = if (selected) AppTheme.colorScheme.tertiary else Color.Transparent

    Box(
        modifier = Modifier
            .padding(all = 12.dp)
            .clip(AppTheme.shapes.small)
            .border(width = borderWidth, shape = AppTheme.shapes.small, color = borderColor)
            .background(color = backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .requiredSize(88.dp)
            .aspectRatio(1f),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = defaultEmoji,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
            )
            Text(
                text = defaultAmount.shortened(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = AppTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun ZapTitle(
    modifier: Modifier = Modifier,
    receiverName: String,
    amount: Long,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                ) {
                    append("ZAP ${receiverName.uppercase()} ")
                }
                withStyle(
                    style = SpanStyle(
                        color = AppTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                    ),
                ) {
                    append("${amount.shortened()} ")
                }
                withStyle(
                    style = SpanStyle(
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                ) {
                    append("SATS")
                }
            },
        )
    }
}

private fun ZappingState.ensureZapConfig(): List<ContentZapConfigItem> {
    return if (this.zapsConfig.size == PRESETS_COUNT) {
        this.zapsConfig
    } else {
        DEFAULT_ZAP_CONFIG
    }
}
