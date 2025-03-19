package net.primal.data.remote.mapper

import net.primal.core.utils.decodeFromStringOrNull
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.serialization.NostrJson
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstEventId
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.findFirstZapRequest

fun List<NostrEvent>.mapAsEventZapDO(profilesMap: Map<String, ProfileData>) =
    mapNotNull { zapReceipt ->
        val zapRequest = zapReceipt.extractZapRequestOrNull()

        val receiverId = zapReceipt.tags.findFirstProfileId()
            ?: return@mapNotNull null

        val senderId = zapRequest?.pubKey
            ?: return@mapNotNull null

        val noteId = zapReceipt.tags.findFirstEventId()
            ?: zapRequest.tags.findFirstEventId()
            ?: return@mapNotNull null

        val amountInSats = 0
        // TODO Bring LnInvoiceUtils to shared
//        val amountInSats = (zapReceipt.tags.findFirstBolt11() ?: zapRequest.tags.findFirstZapAmount())
//            ?.let(LnInvoiceUtils::getAmountInSats)
//            ?: return@mapNotNull null

        val profile = profilesMap[senderId]

        EventZap(
            eventId = noteId,
            zapSenderId = senderId,
            zapSenderDisplayName = profile?.displayName,
            zapSenderHandle = profile?.handle,
            zapSenderInternetIdentifier = profile?.internetIdentifier,
            zapSenderAvatarCdnImage = profile?.avatarCdnImage,
            zapSenderPrimalLegendProfile = profile?.primalPremiumInfo?.legendProfile,
            zapReceiverId = receiverId,
            zapRequestAt = zapRequest.createdAt,
            zapReceiptAt = zapReceipt.createdAt,
            // TODO Bring CurrencyConversionUtils to shared
//            amountInBtc = amountInSats.toBtc(),
            amountInBtc = 0.21,
            message = zapRequest.content,
        )
    }

fun NostrEvent.extractZapRequestOrNull() = NostrJson.decodeFromStringOrNull<NostrEvent>(tags.findFirstZapRequest())
