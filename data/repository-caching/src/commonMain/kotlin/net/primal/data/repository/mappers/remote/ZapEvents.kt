package net.primal.data.repository.mappers.remote

import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.toDouble
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.findFirstATag
import net.primal.domain.nostr.findFirstBolt11
import net.primal.domain.nostr.findFirstEventId
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.findFirstZapAmount
import net.primal.domain.nostr.findFirstZapRequest
import net.primal.domain.nostr.utils.LnInvoiceUtils

fun List<NostrEvent>.mapAsEventZapDO(profilesMap: Map<String, ProfileData>) =
    mapNotNull { zapReceipt ->
        val zapRequest = zapReceipt.extractZapRequestOrNull()

        val receiverId = zapReceipt.tags.findFirstProfileId()
            ?: return@mapNotNull null

        val senderId = zapRequest?.pubKey
            ?: return@mapNotNull null

        val noteId = zapReceipt.tags.findFirstATag()
            ?: zapRequest.tags.findFirstATag()
            ?: zapReceipt.tags.findFirstEventId()
            ?: zapRequest.tags.findFirstEventId()
            ?: return@mapNotNull null

        val amountInSats = (zapReceipt.tags.findFirstBolt11() ?: zapRequest.tags.findFirstZapAmount())
            ?.let(LnInvoiceUtils::getAmountInSats)
            ?: return@mapNotNull null

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
            amountInBtc = amountInSats.toBtc().toDouble(),
            message = zapRequest.content,
        )
    }

fun NostrEvent.extractZapRequestOrNull() = tags.findFirstZapRequest().decodeFromJsonStringOrNull<NostrEvent>()
