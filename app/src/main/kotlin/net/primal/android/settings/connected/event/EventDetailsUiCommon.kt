package net.primal.android.settings.connected.event

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.utils.PrimalDateFormats
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.rememberPrimalFormattedDateTime
import net.primal.android.nostrconnect.ui.NostrEventDetails
import net.primal.android.nostrconnect.ui.buildRows
import net.primal.android.nostrconnect.ui.getStatusTextAndColor
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

@Composable
fun EventDetailsContent(
    modifier: Modifier = Modifier,
    loading: Boolean,
    eventNotSupported: Boolean,
    sessionEvent: SessionEvent?,
    requestTypeId: String?,
    namingMap: Map<String, String>,
    parsedSignedEvent: NostrEvent?,
    parsedUnsignedEvent: NostrUnsignedEvent?,
    rawJson: String?,
) {
    val context = LocalContext.current

    when {
        loading -> PrimalLoadingSpinner()
        !eventNotSupported && sessionEvent != null -> {
            val actionName = requestTypeId?.let { namingMap[it] }
                ?: requestTypeId ?: ""

            val formattedTimestamp = rememberPrimalFormattedDateTime(
                timestamp = sessionEvent.requestedAt,
                format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_SS_A,
            )

            val (statusText, statusColor) = getStatusTextAndColor(context, sessionEvent)

            val rows = remember(sessionEvent, namingMap, parsedSignedEvent, parsedUnsignedEvent) {
                buildRows(
                    context = context,
                    event = sessionEvent,
                    namingMap = namingMap,
                    parsedSignedEvent = parsedSignedEvent,
                    parsedUnsignedEvent = parsedUnsignedEvent,
                )
            }

            NostrEventDetails(
                modifier = modifier,
                title = actionName,
                subtitle = formattedTimestamp,
                rows = rows,
                status = statusText,
                statusColor = statusColor,
                onCopy = { text, label ->
                    copyText(text = text, context = context, label = label)
                },
                footerContent = {
                    if (rawJson != null) {
                        val rawJsonLabel = stringResource(id = R.string.settings_event_details_raw_json_label)
                        PrimalFilledButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .height(50.dp),
                            onClick = {
                                copyText(text = rawJson, context = context, label = rawJsonLabel)
                            },
                        ) {
                            Text(text = stringResource(id = R.string.settings_event_details_copy_raw_json))
                        }
                    }
                },
            )
        }

        eventNotSupported -> {
            ListNoContent(
                modifier = modifier,
                noContentText = stringResource(id = R.string.settings_event_details_not_supported),
                refreshButtonVisible = false,
            )
        }
    }
}
