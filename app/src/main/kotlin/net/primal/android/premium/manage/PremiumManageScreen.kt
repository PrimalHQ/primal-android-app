package net.primal.android.premium.manage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.manage.PremiumManageContract.ManageDestination
import net.primal.android.premium.manage.ui.ManageDestinationTable
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
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
