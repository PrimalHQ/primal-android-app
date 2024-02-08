package net.primal.android.wallet.transactions.send.create.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.transactions.send.create.ui.model.MiningFeeUi
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

@ExperimentalMaterial3Api
@Composable
fun MiningFeeBottomSheet(
    fees: List<MiningFeeUi>,
    selectedFeeIndex: Int,
    onMiningFeeChanged: (MiningFeeUi) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var aboutDialogVisible by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        tonalElevation = 0.dp,
        shape = RectangleShape,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Surface(
            color = AppTheme.extraColorScheme.surfaceVariantAlt2,
            contentColor = AppTheme.colorScheme.onSurfaceVariant,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = R.string.wallet_create_transaction_mining_fee_selector_title),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.titleLarge,
                )

                Spacer(modifier = Modifier.height(32.dp))

                fees.forEachIndexed { index, data ->
                    MiningFeeListItem(
                        data = data,
                        isSelected = index == selectedFeeIndex,
                        onClick = {
                            onMiningFeeChanged(it)
                            onDismissRequest()
                        },
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { aboutDialogVisible = true },
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.wallet_create_transaction_mining_fee_selector_about_text_button,
                        ),
                        color = AppTheme.colorScheme.secondary,
                        style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (aboutDialogVisible) {
        MiningFeeAboutDialog(
            onDismissRequest = { aboutDialogVisible = false },
        )
    }
}

@Composable
private fun MiningFeeListItem(
    data: MiningFeeUi,
    isSelected: Boolean,
    onClick: (MiningFeeUi) -> Unit,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    val maxHeight = OutlinedTextFieldDefaults.MinHeight
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight)
                .padding(horizontal = 32.dp)
                .background(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    shape = AppTheme.shapes.extraLarge,
                )
                .clip(AppTheme.shapes.extraLarge)
                .clickable(enabled = true, onClick = { onClick(data) })
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 1.dp,
                            color = AppTheme.colorScheme.primary,
                            shape = AppTheme.shapes.extraLarge,
                        )
                    } else {
                        Modifier
                    },
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val formattedAmountInSats = numberFormat.format(data.feeInBtc.toSats().toLong())
            Text(
                modifier = Modifier.padding(start = 16.dp),
                style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                color = AppTheme.colorScheme.onPrimary,
                text = "${data.label}: $formattedAmountInSats sats",
            )

            val approxTime = buildAnnotatedString {
                append(stringResource(id = R.string.wallet_create_transaction_mining_fee_selector_approx_label_prefix))
                append(' ')
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(data.confirmationEstimateInMin.toTimeAmount())
                }
            }
            Text(
                modifier = Modifier.padding(end = 16.dp),
                style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                color = AppTheme.colorScheme.onPrimary,
                text = approxTime,
            )
        }
    }
}

@Suppress("MagicNumber")
private fun Int.toTimeAmount(): String {
    return when (this) {
        in (Int.MIN_VALUE..60) -> "$this min"
        in (61..1439) -> "${this / 60} hour"
        else -> "${this / 1440} day"
    }
}

@Composable
private fun MiningFeeAboutDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(id = R.string.wallet_create_transaction_mining_fee_selector_about_title),
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.wallet_create_transaction_mining_fee_selector_about_text),
            )
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(id = android.R.string.ok),
                )
            }
        },
    )
}
