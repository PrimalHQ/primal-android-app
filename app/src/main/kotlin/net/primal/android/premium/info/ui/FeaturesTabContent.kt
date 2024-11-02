package net.primal.android.premium.info.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Check
import net.primal.android.theme.AppTheme
import org.apache.commons.lang3.NotImplementedException


@Composable
fun FeaturesTabContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
) {
    val features = listOf(
        Feature(
            featureText = stringResource(id = R.string.premium_more_info_features_table_first_row),
            free = true,
            premium = true,
        ),
        Feature(
            featureText = stringResource(id = R.string.premium_more_info_features_table_second_row),
            free = true,
            premium = true,
        ),
        Feature(
            featureText = stringResource(id = R.string.premium_more_info_features_table_third_row),
            free = true,
            premium = true,
        ),
        Feature(
            featureText = stringResource(id = R.string.premium_more_info_features_table_fourth_row),
            free = "1 Gb",
            premium = "10 Gb",
        ),
        Feature(
            featureText = stringResource(id = R.string.premium_more_info_features_table_fifth_row),
            free = "100mb",
            premium = "1 Gb",
        ),
        Feature(
            featureText = stringResource(id = R.string.premium_more_info_features_table_sixth_row),
            free = false,
            premium = true,
        ),
        Feature(
            featureText = stringResource(id = R.string.premium_more_info_features_table_seventh_row),
            free = false,
            premium = true,
        ),
        Feature(
            featureText = stringResource(id = R.string.premium_more_info_features_table_eighth_row),
            free = false,
            premium = true,
        ),
        Feature(
            featureText = stringResource(id = R.string.premium_more_info_features_table_ninth_row),
            free = false,
            premium = true,
        ),
        Feature(
            featureText = stringResource(id = R.string.premium_more_info_features_table_tenth_row),
            free = false,
            premium = true,
        ),
    )

    LazyColumn(
        contentPadding = contentPadding,
        modifier = modifier
            .clip(AppTheme.shapes.medium),
    ) {
        item(key = "headerRow") {
            Spacer(modifier = Modifier.height(20.dp))
            FeatureStringRow(
                modifier = Modifier.clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                featureText = stringResource(id = R.string.premium_more_info_features_table_header_feature),
                free = stringResource(id = R.string.premium_more_info_features_table_header_free),
                premium = stringResource(id = R.string.premium_more_info_features_table_header_premium),
                isHeaderRow = true,
            )
        }
        items(
            items = features.dropLast(1),
            key = { it.featureText },
        ) { feature ->
            PrimalDivider()
            feature.ToFeatureRow()
        }
        val lastFeature = features.last()
        item(key = lastFeature.featureText) {
            PrimalDivider()
            lastFeature.ToFeatureRow(
                modifier = Modifier.clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)),
            )
        }
    }
}

@Composable
private fun FeatureStringRow(
    modifier: Modifier = Modifier,
    featureText: String,
    free: String,
    premium: String,
    isHeaderRow: Boolean = false,
) {
    Row(
        modifier = modifier
            .run {
                if (isHeaderRow) {
                    background(AppTheme.extraColorScheme.surfaceVariantAlt1)
                } else {
                    background(AppTheme.extraColorScheme.surfaceVariantAlt3)
                }
            }
            .padding(vertical = 18.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeatureText(featureText = featureText, isHeaderRow = isHeaderRow)
        OfferText(text = free, isHeaderRow = isHeaderRow)
        OfferText(text = premium, isHeaderRow = isHeaderRow)
    }
}

@Composable
private fun RowScope.FeatureText(
    featureText: String,
    isHeaderRow: Boolean = false,
) {
    Text(
        modifier = Modifier
            .padding(end = 16.dp)
            .fillMaxWidth()
            .weight(2f),
        text = featureText,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        fontWeight = if (isHeaderRow) {
            FontWeight.Bold
        } else {
            FontWeight.Normal
        },
        style = AppTheme.typography.bodyLarge,
        fontSize = 18.sp,
    )
}

@Composable
private fun RowScope.OfferText(
    text: String,
    isHeaderRow: Boolean = false,
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        text = text,
        style = AppTheme.typography.bodyLarge,
        maxLines = 1,
        textAlign = TextAlign.Center,
        fontWeight = if (isHeaderRow) {
            FontWeight.Bold
        } else {
            FontWeight.SemiBold
        },
        fontSize = 18.sp,
    )
}

@Composable
private fun FeatureBooleanRow(
    modifier: Modifier = Modifier,
    featureText: String,
    free: Boolean,
    premium: Boolean,
) {
    Row(
        modifier = modifier
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3)
            .padding(vertical = 18.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeatureText(featureText = featureText)
        OfferIcon(value = free)
        OfferIcon(value = premium)
    }

}

@Composable
private fun RowScope.OfferIcon(
    value: Boolean,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        if (value) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = PrimalIcons.Check,
                contentDescription = null,
            )
        } else {
            Spacer(modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun <T : Any> Feature<T>.ToFeatureRow(
    modifier: Modifier = Modifier,
) =
    when {
        this.free is String && this.premium is String ->
            FeatureStringRow(
                modifier = modifier,
                featureText = this.featureText,
                free = this.free,
                premium = this.premium,
            )

        this.free is Boolean && this.premium is Boolean ->
            FeatureBooleanRow(
                modifier = modifier,
                featureText = this.featureText,
                free = this.free,
                premium = this.premium,
            )

        else -> throw NotImplementedException(
            "don't know how to handle types ${this.free::class} and ${this.premium::class}",
        )
    }


data class Feature<T : Any>(
    val featureText: String,
    val free: T,
    val premium: T,
)
