package net.primal.android.core.compose.signer.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.domain.account.model.RequestState
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

val NostrConnectRejectedColor = Color(0xFFFFA02F)

@Composable
fun getStatusTextAndColor(context: Context, event: SessionEvent): Pair<String, Color> {
    return when (event.requestState) {
        RequestState.Approved -> {
            val text = when (event) {
                is SessionEvent.SignEvent -> context.getString(R.string.nostr_connect_status_signed)
                is SessionEvent.Encrypt -> context.getString(R.string.nostr_connect_status_success_encryption)
                is SessionEvent.Decrypt -> context.getString(R.string.nostr_connect_status_success_decryption)
                is SessionEvent.GetPublicKey -> context.getString(R.string.nostr_connect_status_approved)
            }
            text to AppTheme.extraColorScheme.successBright
        }
        RequestState.Rejected -> {
            context.getString(R.string.nostr_connect_status_rejected) to NostrConnectRejectedColor
        }
        else -> "" to Color.Unspecified
    }
}

fun buildRows(
    context: Context,
    event: SessionEvent,
    namingMap: Map<String, String>,
    parsedSignedEvent: NostrEvent? = null,
    parsedUnsignedEvent: NostrUnsignedEvent? = null,
): List<EventDetailRow> {
    return when (event) {
        is SessionEvent.SignEvent -> buildSignEventRows(
            context,
            event,
            namingMap,
            parsedSignedEvent,
            parsedUnsignedEvent,
        )
        is SessionEvent.Encrypt -> buildEncryptRows(context, event)
        is SessionEvent.Decrypt -> buildDecryptRows(context, event)
        is SessionEvent.GetPublicKey -> buildGetPublicKeyRows(context, event)
    }
}

private fun buildEncryptRows(context: Context, event: SessionEvent.Encrypt): List<EventDetailRow> {
    return buildList {
        event.plainText?.let {
            add(
                EventDetailRow.Detail(
                    label = context.getString(R.string.nostr_connect_label_plain_text),
                    value = it,
                    singleLine = false,
                    expandable = true,
                    maxLines = 10,
                ),
            )
        }
        event.encryptedText?.let {
            add(
                EventDetailRow.Detail(
                    label = context.getString(R.string.nostr_connect_label_encrypted_text),
                    value = it,
                    singleLine = false,
                    expandable = true,
                    maxLines = 3,
                ),
            )
        }
    }
}

private fun buildDecryptRows(context: Context, event: SessionEvent.Decrypt): List<EventDetailRow> {
    return buildList {
        event.plainText?.let {
            add(
                EventDetailRow.Detail(
                    label = context.getString(R.string.nostr_connect_label_plain_text),
                    value = it,
                    singleLine = false,
                    expandable = true,
                    maxLines = 10,
                ),
            )
        }
        event.encryptedText?.let {
            add(
                EventDetailRow.Detail(
                    label = context.getString(R.string.nostr_connect_label_encrypted_text),
                    value = it,
                    singleLine = false,
                    expandable = true,
                    maxLines = 3,
                ),
            )
        }
    }
}

private fun buildGetPublicKeyRows(context: Context, event: SessionEvent.GetPublicKey): List<EventDetailRow> {
    return event.publicKey?.let { pubKey ->
        listOf(
            EventDetailRow.Detail(
                label = context.getString(R.string.nostr_connect_label_pubkey),
                value = pubKey,
                isKey = true,
            ),
        )
    } ?: emptyList()
}

private fun buildSignEventRows(
    context: Context,
    event: SessionEvent.SignEvent,
    namingMap: Map<String, String>,
    signedEvent: NostrEvent?,
    unsignedEvent: NostrUnsignedEvent?,
): List<EventDetailRow> {
    val kindName = namingMap[event.requestTypeId] ?: event.requestTypeId
    if (signedEvent != null) {
        return signedEvent.toDetailRows(context, kindName)
    }

    return unsignedEvent?.toDetailRows(context, kindName) ?: emptyList()
}

private fun NostrEvent.toDetailRows(context: Context, kindName: String): List<EventDetailRow> {
    return buildList {
        if (id.isNotBlank()) {
            add(
                EventDetailRow.Detail(
                    label = context.getString(R.string.settings_event_details_id_label),
                    value = id,
                    isKey = true,
                ),
            )
        }
        add(
            EventDetailRow.Detail(
                label = context.getString(R.string.settings_event_details_pubkey_label),
                value = pubKey,
                isKey = true,
            ),
        )
        add(
            EventDetailRow.Detail(
                label = context.getString(R.string.settings_event_details_event_kind_label),
                value = "$kind - $kindName",
            ),
        )
        add(
            EventDetailRow.Detail(
                label = context.getString(R.string.settings_event_details_created_at_label),
                value = createdAt.toString(),
            ),
        )
        if (tags.isNotEmpty()) {
            add(
                EventDetailRow.Tags(
                    label = context.getString(R.string.settings_event_details_tags_label),
                    tags = tags,
                ),
            )
        }
        if (content.isNotBlank()) {
            add(
                EventDetailRow.Detail(
                    label = context.getString(R.string.settings_event_details_content_label),
                    value = content,
                    singleLine = false,
                    expandable = true,
                ),
            )
        }
        if (sig.isNotBlank()) {
            add(
                EventDetailRow.Detail(
                    label = context.getString(R.string.settings_event_details_signature_label),
                    value = sig,
                    isKey = true,
                ),
            )
        }
    }
}

private fun NostrUnsignedEvent.toDetailRows(context: Context, kindName: String): List<EventDetailRow> {
    return buildList {
        add(
            EventDetailRow.Detail(
                label = context.getString(R.string.settings_event_details_pubkey_label),
                value = pubKey,
                isKey = true,
            ),
        )
        add(
            EventDetailRow.Detail(
                label = context.getString(R.string.settings_event_details_event_kind_label),
                value = "$kind - $kindName",
            ),
        )
        add(
            EventDetailRow.Detail(
                label = context.getString(R.string.settings_event_details_created_at_label),
                value = createdAt.toString(),
            ),
        )
        if (tags.isNotEmpty()) {
            add(
                EventDetailRow.Tags(
                    label = context.getString(R.string.settings_event_details_tags_label),
                    tags = tags,
                ),
            )
        }
        if (content.isNotBlank()) {
            add(
                EventDetailRow.Detail(
                    label = context.getString(R.string.settings_event_details_content_label),
                    value = content,
                    singleLine = false,
                    expandable = true,
                ),
            )
        }
    }
}
