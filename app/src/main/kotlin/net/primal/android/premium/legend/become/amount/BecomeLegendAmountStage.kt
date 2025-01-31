package net.primal.android.premium.legend.become.amount

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Year
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalSliderThumb
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.LegendaryStyle
import net.primal.android.premium.legend.become.BecomeLegendBottomBarButton
import net.primal.android.premium.legend.become.PremiumBecomeLegendContract
import net.primal.android.premium.legend.become.PremiumBecomeLegendContract.UiState
import net.primal.android.premium.legend.become.PrimalLegendAmount
import net.primal.android.premium.ui.PremiumBadge
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun BecomeLegendAmountStage(
    modifier: Modifier,
    state: UiState,
    eventPublisher: (PremiumBecomeLegendContract.UiEvent) -> Unit,
    onClose: () -> Unit,
    onNext: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_become_legend_primal_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                showDivider = false,
            )
        },
        bottomBar = {
            BecomeLegendBottomBarButton(
                text = stringResource(R.string.premium_become_legend_button_pay_now),
                onClick = onNext,
                enabled = state.arePaymentInstructionsAvailable(),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                UniversalAvatarThumbnail(
                    avatarCdnImage = state.avatarCdnImage,
                    avatarSize = 80.dp,
                    legendaryCustomization = LegendaryCustomization(
                        avatarGlow = true,
                        legendaryStyle = LegendaryStyle.GOLD,
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
                NostrUserText(
                    modifier = Modifier.padding(start = 8.dp),
                    displayName = state.primalName ?: "",
                    internetIdentifier = "${state.primalName}@primal.net",
                    internetIdentifierBadgeSize = 24.dp,
                    fontSize = 20.sp,
                    legendaryCustomization = LegendaryCustomization(
                        customBadge = true,
                        legendaryStyle = LegendaryStyle.GOLD,
                    ),
                )
            }

            PremiumBadge(
                firstCohort = "Legend",
                secondCohort = Year.now().value.toString(),
                membershipExpired = false,
                legendaryStyle = LegendaryStyle.GOLD,
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (state.arePaymentInstructionsAvailable() || state.isFetchingPaymentInstructions) {
                SelectAmountSlider(
                    state = state,
                    eventPublisher = eventPublisher,
                )
            } else {
                NoPaymentInstructionsColumn(
                    onRetryClick = { eventPublisher(PremiumBecomeLegendContract.UiEvent.FetchPaymentInstructions) },
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun NoPaymentInstructionsColumn(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = stringResource(R.string.premium_become_legend_failed_fetching_payment_instructions),
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
        TextButton(
            onClick = onRetryClick,
        ) {
            Text(
                text = stringResource(R.string.premium_become_legend_retry_button),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.primary,
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun SelectAmountSlider(state: UiState, eventPublisher: (PremiumBecomeLegendContract.UiEvent) -> Unit) {
    var slideValue by remember { mutableFloatStateOf(state.selectedAmountInBtc.toFloat()) }

    PrimalLegendAmount(
        btcValue = state.selectedAmountInBtc,
        exchangeBtcUsdRate = state.exchangeBtcUsdRate,
    )

    Column(
        modifier = Modifier.padding(horizontal = 10.dp),
    ) {
        val sliderColors = sliderColors(value = slideValue.toInt())
        val interactionSource = remember { MutableInteractionSource() }
        Slider(
            modifier = Modifier.fillMaxWidth(),
            interactionSource = interactionSource,
            colors = sliderColors,
            track = {
                SliderDefaults.Track(
                    sliderState = it,
                    modifier = Modifier.scale(scaleX = 1f, scaleY = 0.35f),
                    colors = sliderColors,
                    drawStopIndicator = null,
                    drawTick = { _, _ -> },
                    thumbTrackGapSize = 0.dp,
                )
            },
            thumb = {
                PrimalSliderThumb(
                    interactionSource = interactionSource,
                    colors = sliderColors,
                )
            },
            value = slideValue,
            onValueChange = {
                slideValue = it
                eventPublisher(PremiumBecomeLegendContract.UiEvent.UpdateSelectedAmount(newAmount = it))
            },
            steps = (state.maxLegendThresholdInBtc - state.minLegendThresholdInBtc).toInt(),
            valueRange = state.minLegendThresholdInBtc.toFloat()..state.maxLegendThresholdInBtc.toFloat(),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "$1000",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )

            Text(
                text = "1 BTC",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }
    }
}

@Composable
fun MainAmountText(
    modifier: Modifier,
    amount: String,
    currency: String,
    textSize: TextUnit = 42.sp,
    amountColor: Color = Color.Unspecified,
    currencyColor: Color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = amount,
            textAlign = TextAlign.Center,
            style = AppTheme.typography.displayMedium,
            fontSize = textSize,
            color = amountColor,
        )

        Text(
            modifier = Modifier.padding(bottom = (textSize.value / 6).dp),
            text = " ${currency.trim()}",
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = AppTheme.typography.bodyMedium,
            color = currencyColor,
        )
    }
}

@Composable
fun AltAmountText(
    modifier: Modifier,
    amount: String,
    currency: String,
    textSize: TextUnit = 18.sp,
    amountColor: Color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
    currencyColor: Color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = amount,
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyMedium,
            fontSize = textSize,
            color = amountColor,
        )

        Text(
            modifier = Modifier,
            text = " ${currency.trim()}",
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = AppTheme.typography.bodyMedium,
            fontSize = textSize,
            color = currencyColor,
        )
    }
}

@Composable
private fun sliderColors(value: Int) =
    SliderDefaults.colors(
        thumbColor = if (value == 0) {
            AppTheme.colorScheme.outline
        } else {
            AppTheme.extraColorScheme.onSurfaceVariantAlt2
        },
        activeTrackColor = AppTheme.colorScheme.tertiary,
        activeTickColor = AppTheme.colorScheme.tertiary,
        inactiveTrackColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        inactiveTickColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
    )
