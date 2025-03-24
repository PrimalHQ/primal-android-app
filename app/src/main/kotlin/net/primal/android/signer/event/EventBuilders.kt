package net.primal.android.signer.event

import net.primal.android.networking.UserAgentProvider
import net.primal.android.nostr.ext.extractProfileId
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.core.utils.serialization.CommonJson
import net.primal.data.remote.api.settings.model.AppSettingsDescription
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.asIdentifierTag

fun buildAppSpecificDataEvent(pubkey: String) =
    NostrUnsignedEvent(
        pubKey = pubkey.extractProfileId() ?: "",
        kind = NostrEventKind.ApplicationSpecificData.value,
        tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
        content = CommonJson.encodeToString(
            AppSettingsDescription(description = "Sync app settings"),
        ),
    )
