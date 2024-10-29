package net.primal.android.premium.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.NostrichFilled
import net.primal.android.core.compose.icons.primaliconpack.PrimalPremiumDark
import net.primal.android.core.compose.icons.primaliconpack.VerifiedFilled
import net.primal.android.theme.AppTheme

private val OrColor = Color(0xFF020101)

@Composable
fun PremiumHomeStage(onClose: () -> Unit, onFindPrimalName: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .systemBarsPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
    ) {
        Icon(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
                .size(58.dp),
            imageVector = PrimalIcons.PrimalPremiumDark,
            contentDescription = null,
            tint = Color.Unspecified,
        )
        Text(
            text = stringResource(id = R.string.premium_subscribe_to_get),
            color = AppTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        PremiumOfferCard(
            modifier = Modifier.padding(bottom = 16.dp),
            onLearnMoreClick = {},
        )
        PriceRow()
        ButtonsColumn(
            modifier = Modifier.padding(16.dp),
            onClose = onClose,
            onFindPrimalName = onFindPrimalName,
        )
    }
}

@Composable
private fun PremiumOfferCard(modifier: Modifier = Modifier, onLearnMoreClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.large)
            .border(width = 1.dp, color = AppTheme.extraColorScheme.surfaceVariantAlt1)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt2),
    ) {
        PrimalNameRow(
            modifier = Modifier
                .padding(top = 32.dp)
                .padding(horizontal = 24.dp),
        )
        NostrToolsRow(
            modifier = Modifier.padding(24.dp),
        )
        LearnMoreSection(
            onLearnMoreClick = onLearnMoreClick,
        )
    }
}

@Composable
private fun PrimalNameRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.Start),
    ) {
        IconBadge(
            modifier = Modifier.padding(horizontal = 7.dp),
            size = 72.dp,
            imageVector = PrimalIcons.VerifiedFilled,
        )
        DescriptionSection(
            headerText = stringResource(id = R.string.premium_primal_name),
            bulletPoints = listOf(
                stringResource(id = R.string.premium_primal_name_benefit_one),
                stringResource(id = R.string.premium_primal_name_benefit_two),
                stringResource(id = R.string.premium_primal_name_benefit_three),
            ),
        )
    }
}

@Composable
private fun DescriptionSection(headerText: String, bulletPoints: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = headerText,
            color = AppTheme.colorScheme.onBackground,
            style = AppTheme.typography.bodyLarge,
            fontSize = TextUnit(25f, TextUnitType.Sp),
            fontWeight = FontWeight.Bold,
        )
        bulletPoints.onEach {
            Text(
                text = "• $it",
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                style = AppTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun IconBadge(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    size: Dp,
) {
    Icon(
        modifier = modifier.size(size),
        imageVector = imageVector,
        contentDescription = null,
        tint = Color.Unspecified,
    )
}

@Composable
private fun NostrToolsRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.Start),
    ) {
        IconBadge(
            size = 86.dp,
            imageVector = PrimalIcons.NostrichFilled,
        )
        DescriptionSection(
            headerText = stringResource(id = R.string.premium_nostr_tools),
            bulletPoints = listOf(
                stringResource(id = R.string.premium_nostr_tools_benefit_one),
                stringResource(id = R.string.premium_nostr_tools_benefit_two),
                stringResource(id = R.string.premium_nostr_tools_benefit_three),
                stringResource(id = R.string.premium_nostr_tools_benefit_four),
            ),
        )
    }
}

@Composable
private fun LearnMoreSection(onLearnMoreClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.extraColorScheme.surfaceVariantAlt1),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .clickable { onLearnMoreClick() },
            text = stringResource(id = R.string.premium_learn_more),
            color = AppTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun PriceRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
    ) {
        PricePeriodColumn(
            price = stringResource(id = R.string.premium_monthly_price),
            period = stringResource(id = R.string.premium_period_monthly),
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AppTheme.extraColorScheme.onSurfaceVariantAlt3),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(id = R.string.premium_price_or), color = OrColor, fontWeight = FontWeight.Bold)
        }
        PricePeriodColumn(
            price = stringResource(id = R.string.premium_annually_price),
            period = stringResource(id = R.string.premium_period_annually),
        )
    }
}

@Composable
private fun PricePeriodColumn(price: String, period: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = price,
            color = AppTheme.colorScheme.onBackground,
            style = AppTheme.typography.bodyLarge,
            fontSize = TextUnit(22f, TextUnitType.Sp),
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = period,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ButtonsColumn(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onFindPrimalName: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.premium_start_by_reserving_primal_name),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )
        PrimalFilledButton(
            modifier = Modifier.fillMaxWidth(),
            height = 58.dp,
            onClick = onFindPrimalName,
        ) {
            Text(
                text = stringResource(id = R.string.premium_find_primal_name_button),
                color = AppTheme.colorScheme.onBackground,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        TextButton(
            modifier = Modifier
                .height(58.dp)
                .fillMaxWidth(),
            onClick = onClose,
        ) {
            Text(
                text = stringResource(id = R.string.premium_cancel_button),
                color = AppTheme.colorScheme.onBackground,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}
