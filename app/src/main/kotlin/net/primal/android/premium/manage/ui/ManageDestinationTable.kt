package net.primal.android.premium.manage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.premium.manage.PremiumManageContract
import net.primal.android.theme.AppTheme

@Composable
fun ManageDestinationTable(
    modifier: Modifier = Modifier,
    title: String,
    destinations: List<PremiumManageContract.ManageDestination>,
    onDestination: (PremiumManageContract.ManageDestination) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            modifier = Modifier.padding(start = 6.dp),
            text = title,
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        Column(
            modifier = modifier
                .clip(AppTheme.shapes.large)
                .background(AppTheme.extraColorScheme.surfaceVariantAlt3),
        ) {
            destinations.dropLast(1).forEach { destination ->
                DestinationRow(
                    destination = destination,
                    onClick = { onDestination(destination) },
                )
                PrimalDivider()
            }
            DestinationRow(
                destination = destinations.last(),
                onClick = { onDestination(destinations.last()) },
            )
        }
    }
}

@Composable
private fun DestinationRow(
    modifier: Modifier = Modifier,
    destination: PremiumManageContract.ManageDestination,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(18.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = destination.toDisplayString(),
            style = AppTheme.typography.bodyLarge,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        )
    }
}

@Composable
private fun PremiumManageContract.ManageDestination.toDisplayString(): String =
    when (this) {
        PremiumManageContract.ManageDestination.MediaManagement ->
            stringResource(id = R.string.premium_manage_media_management)

        PremiumManageContract.ManageDestination.PremiumRelay ->
            stringResource(id = R.string.premium_manage_premium_relay)

        PremiumManageContract.ManageDestination.ContactListBackup ->
            stringResource(id = R.string.premium_manage_contact_list_backup)

        PremiumManageContract.ManageDestination.ContentBackup ->
            stringResource(id = R.string.premium_manage_content_backup)

        PremiumManageContract.ManageDestination.ManageSubscription ->
            stringResource(id = R.string.premium_manage_manage_subscription)

        PremiumManageContract.ManageDestination.ChangePrimalName ->
            stringResource(id = R.string.premium_manage_change_primal_name)

        PremiumManageContract.ManageDestination.ExtendSubscription ->
            stringResource(id = R.string.premium_manage_extend_subscription)

        PremiumManageContract.ManageDestination.LegendaryProfileCustomization ->
            stringResource(id = R.string.premium_manage_legendary_customization)
    }
