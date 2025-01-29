package net.primal.android.settings.wallet.connection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalCircleButton
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NwcDailyBudgetBottomSheet(
    initialDailyBudget: String?,
    onDismissRequest: () -> Unit,
    onBudgetSelected: (String?) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()

    val budgetOptions = listOf("1000", "10000", "100000", "1000000", "no limit")

    val initialSelection = initialDailyBudget?.takeIf { it != "no limit" }
    val selectedBudget = remember { mutableStateOf(initialSelection) }

    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        tonalElevation = 0.dp,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        contentColor = AppTheme.colorScheme.onSurfaceVariant,
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(text = stringResource(id = R.string.settings_wallet_nwc_connections_header_daily_budget))
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
            ),
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            budgetOptions.forEach { option ->
                BudgetOptionRow(
                    option = option,
                    isSelected = selectedBudget.value == (option.takeIf { it != "no limit" }),
                    onSelect = { selectedBudget.value = option.takeIf { it != "no limit" } },
                )
            }

            if (initialDailyBudget != null && initialDailyBudget !in budgetOptions) {
                BudgetOptionRow(
                    option = initialDailyBudget,
                    isSelected = selectedBudget.value == initialDailyBudget,
                    onSelect = { selectedBudget.value = initialDailyBudget },
                )
            }

            PrimalCircleButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(56.dp),
                onClick = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismissRequest()
                        }
                        onBudgetSelected(selectedBudget.value)
                    }
                },
            ) {
                Text(
                    text = stringResource(
                        id = R.string.settings_wallet_new_nwc_connection_bottom_sheet_pick_daily_budget_save,
                    ),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun BudgetOptionRow(
    option: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .clip(RoundedCornerShape(12.dp)),
        headlineContent = {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = option.toIntOrNull()?.let { "%,d sats".format(it) } ?: option,
                style = AppTheme.typography.bodyMedium,
            )
        },
        trailingContent = {
            if (isSelected) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        ),
    )
}
