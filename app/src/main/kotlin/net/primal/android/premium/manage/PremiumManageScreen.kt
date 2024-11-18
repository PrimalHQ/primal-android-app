package net.primal.android.premium.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.manage.PremiumManageContract.ManageDestination
import net.primal.android.theme.AppTheme

@Composable
fun PremiumManageScreen(
    viewModel: PremiumManageViewModel,
    onFAQClick: () -> Unit,
    onDestination: (ManageDestination) -> Unit,
    onClose: () -> Unit,
) {
    val state = viewModel.state.collectAsState()

    PremiumManageScreen(
        state = state.value,
        onFAQClick = onFAQClick,
        onDestination = onDestination,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumManageScreen(
    state: PremiumManageContract.UiState,
    onFAQClick: () -> Unit,
    onDestination: (ManageDestination) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_manage_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(20.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ManageDestinationTable(
                title = stringResource(id = R.string.premium_nostr_tools),
                destinations = listOf(
                    ManageDestination.MediaManagement,
                    ManageDestination.PremiumRelay,
                    ManageDestination.ContactListBackup,
                    ManageDestination.ContentBackup,
                ),
                onDestination = onDestination,
            )

            ManageDestinationTable(
                title = stringResource(id = R.string.premium_manage_primal_account),
                destinations = listOfNotNull(
                    ManageDestination.ManageSubscription,
                    ManageDestination.ChangePrimalName,
                    if (!state.isLegend && !state.isRecurring) ManageDestination.ExtendSubscription else null,
                    if (state.isLegend) {
                        ManageDestination.LegendaryProfileCustomization
                    } else {
                        ManageDestination.BecomeALegend
                    },
                ),
                onDestination = onDestination,
            )

            FAQNoticeRow(onFAQClick = onFAQClick)
        }
    }
}

@Composable
fun FAQNoticeRow(onFAQClick: () -> Unit) {
    Row {
        Text(
            text = stringResource(id = R.string.premium_manage_have_a_question) + " ",
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            style = AppTheme.typography.bodyLarge,
        )
        Text(
            modifier = Modifier.clickable { onFAQClick() },
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(AppTheme.colorScheme.secondary)) {
                    append(stringResource(id = R.string.premium_manage_check_faq))
                }
                append(".")
            },
        )
    }
}

@Composable
fun ManageDestinationTable(
    modifier: Modifier = Modifier,
    title: String,
    destinations: List<ManageDestination>,
    onDestination: (ManageDestination) -> Unit,
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
    destination: ManageDestination,
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
private fun ManageDestination.toDisplayString(): String =
    when (this) {
        ManageDestination.MediaManagement ->
            stringResource(id = R.string.premium_manage_media_management)

        ManageDestination.PremiumRelay ->
            stringResource(id = R.string.premium_manage_premium_relay)

        ManageDestination.ContactListBackup ->
            stringResource(id = R.string.premium_manage_contact_list_backup)

        ManageDestination.ContentBackup ->
            stringResource(id = R.string.premium_manage_content_backup)

        ManageDestination.ManageSubscription ->
            stringResource(id = R.string.premium_manage_manage_subscription)

        ManageDestination.ChangePrimalName ->
            stringResource(id = R.string.premium_manage_change_primal_name)

        ManageDestination.ExtendSubscription ->
            stringResource(id = R.string.premium_manage_extend_subscription)

        ManageDestination.LegendaryProfileCustomization ->
            stringResource(id = R.string.premium_manage_legendary_customization)

        ManageDestination.BecomeALegend ->
            stringResource(R.string.premium_manage_become_a_legend)
    }
