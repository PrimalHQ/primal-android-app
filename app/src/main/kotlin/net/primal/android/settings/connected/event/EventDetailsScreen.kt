package net.primal.android.settings.connected.event

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.PrimalDateFormats
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.rememberPrimalFormattedDateTime
import net.primal.android.nostrconnect.ui.NostrEventDetails
import net.primal.android.nostrconnect.ui.buildRows
import net.primal.android.nostrconnect.ui.getStatusTextAndColor

@Composable
fun EventDetailsScreen(viewModel: EventDetailsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()
    EventDetailsScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(state: EventDetailsContract.UiState, onClose: () -> Unit) {
    val context = LocalContext.current
    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_event_details_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            when {
                state.loading -> PrimalLoadingSpinner()
                !state.eventNotSupported && state.sessionEvent != null -> {
                    val event = state.sessionEvent
                    val actionName = state.requestTypeId?.let { state.namingMap[it] }
                        ?: state.requestTypeId ?: ""

                    val formattedTimestamp = rememberPrimalFormattedDateTime(
                        timestamp = event.requestedAt,
                        format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_SS_A,
                    )

                    val (statusText, statusColor) = getStatusTextAndColor(context, event)

                    val rows = remember(event, state.namingMap, state.parsedSignedEvent, state.parsedUnsignedEvent) {
                        buildRows(
                            context = context,
                            event = event,
                            namingMap = state.namingMap,
                            parsedSignedEvent = state.parsedSignedEvent,
                            parsedUnsignedEvent = state.parsedUnsignedEvent,
                        )
                    }

                    NostrEventDetails(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        title = actionName,
                        subtitle = formattedTimestamp,
                        rows = rows,
                        status = statusText,
                        statusColor = statusColor,
                        onCopy = { text, label ->
                            copyText(text = text, context = context, label = label)
                        },
                        footerContent = {
                            if (state.rawJson != null) {
                                val rawJsonLabel = stringResource(id = R.string.settings_event_details_raw_json_label)
                                PrimalFilledButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                        .height(50.dp),
                                    onClick = {
                                        copyText(text = state.rawJson, context = context, label = rawJsonLabel)
                                    },
                                ) {
                                    Text(text = stringResource(id = R.string.settings_event_details_copy_raw_json))
                                }
                            }
                        },
                    )
                }
                state.eventNotSupported -> {
                    ListNoContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        noContentText = stringResource(id = R.string.settings_event_details_not_supported),
                        refreshButtonVisible = false,
                    )
                }
            }
        },
    )
}
