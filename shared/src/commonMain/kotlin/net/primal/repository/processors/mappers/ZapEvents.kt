package net.primal.repository.processors.mappers

import net.primal.db.events.EventZap
import net.primal.db.profiles.ProfileData
import net.primal.networking.model.NostrEvent
import net.primal.repository.findFirstEventId
import net.primal.repository.findFirstProfileId
import net.primal.repository.findFirstZapRequest
import net.primal.serialization.json.NostrJson
import net.primal.serialization.json.decodeFromStringOrNull


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
