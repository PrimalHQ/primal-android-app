package net.primal.android.settings.zaps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

@Composable
fun ZapSettingsScreen(viewModel: ZapSettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()
    ZapSettingsScreen(
        uiState = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZapSettingsScreen(
    uiState: ZapSettingsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (ZapSettingsContract.UiEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_zaps_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                item {
                    Text(
                        modifier = Modifier.padding(bottom = 16.dp),
                        text = stringResource(
                            id = R.string.settings_zaps_default_zap_amount_header,
                        ).uppercase(),
                        style = AppTheme.typography.bodySmall,
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(fraction = 0.3f),
                        value = uiState.defaultZapAmount?.toString() ?: "",
                        onValueChange = {
                            if (it.isDigitsOnly()) {
                                eventPublisher(
                                    ZapSettingsContract.UiEvent.ZapDefaultAmountChanged(
                                        newAmount = it.toULongOrNull(),
                                    ),
                                )
                            }
                        },
                        enabled = true,
                        textStyle = AppTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            autoCorrect = false,
                        ),
                        shape = AppTheme.shapes.small,
                        colors = zapTextFieldColors(),
                    )

                    PrimalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    )

                    Text(
                        modifier = Modifier.padding(bottom = 16.dp),
                        text = stringResource(
                            id = R.string.settings_zaps_custom_zaps_header,
                        ).uppercase(),
                        style = AppTheme.typography.bodySmall,
                    )

                    ZapOptionDashboard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        zapOptions = uiState.zapOptions,
                        editable = true,
                        onZapOptionsChanged = {
                            eventPublisher(
                                ZapSettingsContract.UiEvent.ZapOptionsChanged(
                                    newOptions = it,
                                ),
                            )
                        },
                    )
                }
            }
        },
    )
}

@Composable
fun ZapOptionDashboard(
    modifier: Modifier,
    zapOptions: List<ULong?>,
    editable: Boolean = true,
    zapEmojis: List<String> = listOf(
        "\uD83D\uDC4D",
        "\uD83C\uDF3F",
        "\uD83E\uDD19",
        "\uD83D\uDC9C",
        "\uD83D\uDD25",
        "\uD83D\uDE80",
    ),
    onZapOptionsChanged: (List<ULong?>) -> Unit,
) {
    Row(
        modifier = modifier,
    ) {
        ZapOption(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            emoji = zapEmojis[0],
            amount = zapOptions.getOrNull(0),
            editable = editable,
            onZapAmountChanged = {
                onZapOptionsChanged(zapOptions.copy(index = 0, amount = it))
            },
        )

        ZapOption(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            emoji = zapEmojis[1],
            amount = zapOptions.getOrNull(1),
            editable = editable,
            onZapAmountChanged = {
                onZapOptionsChanged(zapOptions.copy(index = 1, amount = it))
            },
        )
        ZapOption(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            emoji = zapEmojis[2],
            amount = zapOptions.getOrNull(2),
            editable = editable,
            onZapAmountChanged = {
                onZapOptionsChanged(zapOptions.copy(index = 2, amount = it))
            },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        ZapOption(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            emoji = zapEmojis[3],
            amount = zapOptions.getOrNull(3),
            editable = editable,
            onZapAmountChanged = {
                onZapOptionsChanged(zapOptions.copy(index = 3, amount = it))
            },
        )

        ZapOption(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            emoji = zapEmojis[4],
            amount = zapOptions.getOrNull(4),
            editable = editable,
            onZapAmountChanged = {
                onZapOptionsChanged(zapOptions.copy(index = 4, amount = it))
            },
        )
        ZapOption(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            emoji = zapEmojis[5],
            amount = zapOptions.getOrNull(5),
            editable = editable,
            onZapAmountChanged = {
                onZapOptionsChanged(zapOptions.copy(index = 5, amount = it))
            },
        )
    }
}

private fun List<ULong?>.copy(index: Int, amount: ULong?): List<ULong?> {
    val newOptions = this.toMutableList()
    newOptions[index] = amount
    return newOptions
}

@Composable
fun ZapOption(
    modifier: Modifier,
    emoji: String,
    amount: ULong?,
    editable: Boolean = true,
    onZapAmountChanged: (ULong?) -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier
                .background(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    shape = AppTheme.shapes.small,
                )
                .border(
                    width = 1.dp,
                    color = AppTheme.colorScheme.outline,
                    shape = AppTheme.shapes.small.copy(
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp),
                    ),
                )
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 20.dp),
            text = emoji,
            textAlign = TextAlign.Center,
            fontSize = 28.sp,
        )

        OutlinedTextField(
            modifier = Modifier.padding(top = 4.dp),
            value = amount?.toString() ?: "",
            onValueChange = {
                if (it.isDigitsOnly()) {
                    onZapAmountChanged(it.toULongOrNull())
                }
            },
            enabled = editable,
            textStyle = AppTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            ),
            shape = AppTheme.shapes.small.copy(
                topStart = CornerSize(0.dp),
                topEnd = CornerSize(0.dp),
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                autoCorrect = false,
            ),
            colors = zapTextFieldColors(),
        )
    }
}

@Composable
private fun zapTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        focusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        disabledContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        unfocusedBorderColor = AppTheme.colorScheme.outline,
        focusedBorderColor = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
        disabledBorderColor = AppTheme.colorScheme.outline,
        disabledTextColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        focusedTextColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        unfocusedTextColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
    )
