package net.primal.android.premium.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.format.FormatStyle
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.utils.formatToDefaultDateFormat
import net.primal.android.premium.domain.PremiumMembership
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.theme.AppTheme

@Composable
fun PrimalPremiumTable(
    modifier: Modifier = Modifier,
    profileNostrAddress: String?,
    profileLightningAddress: String?,
    premiumMembership: PremiumMembership,
    onApplyPrimalNostrAddress: () -> Unit,
    onApplyPrimalLightningAddress: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.large)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3),
    ) {
        PrimalPremiumTableRow(
            key = stringResource(id = R.string.premium_primal_name_nostr_address),
            value = profileNostrAddress ?: stringResource(id = R.string.premium_primal_name_not_set_value),
            onApplyClick = onApplyPrimalNostrAddress,
            primalPremiumValue = premiumMembership.nostrAddress,
        )
        PrimalDivider()
        PrimalPremiumTableRow(
            key = stringResource(id = R.string.premium_primal_name_lightning_address),
            value = profileLightningAddress ?: stringResource(id = R.string.premium_primal_name_not_set_value),
            onApplyClick = onApplyPrimalLightningAddress,
            primalPremiumValue = premiumMembership.lightningAddress,
        )
        PrimalDivider()
        PrimalPremiumTableRow(
            key = stringResource(id = R.string.premium_primal_name_vip_profile),
            value = premiumMembership.vipProfile.stripUrlProtocol(),
            alwaysHideApply = true,
        )
        PrimalDivider()
        if (premiumMembership.isPrimalLegendTier()) {
            PrimalPremiumTableRow(
                key = stringResource(id = R.string.premium_home_table_expires),
                value = stringResource(id = R.string.premium_home_table_never),
                alwaysHideApply = true,
            )
        } else {
            PrimalPremiumTableRow(
                key = when {
                    premiumMembership.isExpired() -> stringResource(id = R.string.premium_home_table_expired_on)
                    premiumMembership.recurring -> stringResource(id = R.string.premium_home_table_renews_on)
                    else -> stringResource(id = R.string.premium_home_table_expires_on)
                },
                value = when {
                    premiumMembership.recurring && premiumMembership.renewsOn != null ->
                        Instant.ofEpochSecond(premiumMembership.renewsOn)
                            .formatToDefaultDateFormat(FormatStyle.LONG)

                    premiumMembership.expiresOn != null -> Instant.ofEpochSecond(premiumMembership.expiresOn)
                        .formatToDefaultDateFormat(FormatStyle.LONG)

                    else -> stringResource(R.string.premium_home_table_never)
                },
                alwaysHideApply = true,
            )
        }
    }
}

@Composable
private fun PrimalPremiumTableRow(
    modifier: Modifier = Modifier,
    key: String,
    value: String,
    alwaysHideApply: Boolean = false,
    onApplyClick: (() -> Unit)? = null,
    primalPremiumValue: String? = null,
) {
    val shouldShowApply = value != primalPremiumValue && !alwaysHideApply
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .padding(end = 8.dp)
                .weight(1f),
            text = key.run {
                if (shouldShowApply) {
                    key.replace(" ", "\n")
                } else {
                    key
                }
            },
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 16.sp,
        )
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = value,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
            )
            if (shouldShowApply) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    primalPremiumValue?.let {
                        Text(
                            text = primalPremiumValue,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                            style = AppTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        modifier = Modifier.clickable { onApplyClick?.invoke() },
                        text = stringResource(id = R.string.premium_home_table_apply),
                        color = AppTheme.colorScheme.secondary,
                        style = AppTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

private fun String.stripUrlProtocol() = this.dropWhile { it != ':' }.drop(n = 3)
