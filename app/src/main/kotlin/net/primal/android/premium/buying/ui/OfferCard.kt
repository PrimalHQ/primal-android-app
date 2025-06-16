package net.primal.android.premium.buying.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Check
import net.primal.android.theme.AppTheme

@Composable
fun OfferCard(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    titleSuffix: String,
    titleColor: Color,
    autoSizePrice: Boolean,
    priceText: String,
    priceFontSize: TextUnit? = null,
    priceFontSizeResolved: ((TextUnit) -> Unit)? = null,
    billingText: String,
    badgeColor: Color,
    descriptionItems: List<String>,
    hideFirstBullet: Boolean = false,
    buttonText: String,
    onBuyOfferClick: () -> Unit,
    badgeText: String?,
    buttonColor: Color = AppTheme.colorScheme.primary,
) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.medium)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3)
            .padding(paddingValues),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            OfferTitle(
                titleSuffix = titleSuffix,
                titleColor = titleColor,
            )
            Spacer(Modifier.height(16.dp))

            OfferPrice(
                priceText = priceText,
                fontSize = priceFontSize ?: 44.sp,
                autoSize = autoSizePrice,
                priceFontSizeResolved = { priceFontSizeResolved?.invoke(it) },

            )
            Spacer(Modifier.height(4.dp))

            BillingInfo(
                billingText = billingText,
                badgeColor = badgeColor,
                badgeText = badgeText,
            )
            Spacer(Modifier.height(16.dp))

            DescriptionSection(
                bulletPoints = descriptionItems,
                showFirstWithoutCheckmark = hideFirstBullet,
            )
        }

        PrimalFilledButton(
            modifier = Modifier.fillMaxWidth(),
            height = 48.dp,
            containerColor = buttonColor,
            onClick = onBuyOfferClick,
        ) {
            Text(
                text = buttonText,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun OfferTitle(titleSuffix: String, titleColor: Color) {
    Row {
        Text(
            text = "Primal",
            fontSize = 24.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colorScheme.onSurface,
        )
        Text(
            text = " $titleSuffix",
            fontSize = 24.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor,
        )
    }
}

@Suppress("MagicNumber")
@Composable
private fun OfferPrice(
    priceText: String,
    autoSize: Boolean,
    fontSize: TextUnit,
    priceFontSizeResolved: (TextUnit) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        BasicText(
            modifier = Modifier.weight(weight = 1f, fill = false),
            text = priceText,
            style = TextStyle(
                fontSize = fontSize,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onSurface,
            ),
            autoSize = if (autoSize) {
                TextAutoSize.StepBased(
                    minFontSize = 28.sp,
                    maxFontSize = 44.sp,
                    stepSize = 2.sp,
                )
            } else {
                null
            },
            maxLines = 1,
            onTextLayout = { result ->
                priceFontSizeResolved(result.layoutInput.style.fontSize)
            },
        )
        Text(
            text = stringResource(R.string.subscription_price_per_month),
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Normal,
            color = AppTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

private const val BadgeCornerRadiusPercent = 50

@Composable
private fun BillingInfo(
    billingText: String,
    badgeColor: Color,
    badgeText: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(weight = 1f, fill = false),
            text = billingText,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
        Spacer(Modifier.width(4.dp))

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .background(
                        color = badgeColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(BadgeCornerRadiusPercent),
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun DescriptionSection(showFirstWithoutCheckmark: Boolean = false, bulletPoints: List<String>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        items(
            count = bulletPoints.size,
        ) { index ->
            val item = bulletPoints[index]

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!(showFirstWithoutCheckmark && index == 0)) {
                    Icon(
                        modifier = Modifier.size(10.dp),
                        imageVector = PrimalIcons.Check,
                        contentDescription = null,
                        tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    )
                }
                Text(
                    text = item,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}
