package net.primal.android.signer.client.event

import net.primal.android.networking.UserAgentProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.utils.extractProfileId
import net.primal.domain.settings.AppSettingsDescription

fun buildAppSpecificDataEvent(pubkey: String) =
    NostrUnsignedEvent(
        pubKey = pubkey.extractProfileId() ?: "",
        kind = NostrEventKind.ApplicationSpecificData.value,
        tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
        content = AppSettingsDescription(description = "Sync app settings").encodeToJsonString(),
    )
