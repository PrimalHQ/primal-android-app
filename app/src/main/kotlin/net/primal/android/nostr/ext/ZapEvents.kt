package net.primal.android.nostr.ext

import net.primal.android.events.db.EventZap
import net.primal.android.profile.db.ProfileData
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.utils.LnInvoiceUtils
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.nostr.NostrEvent
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
            amountInBtc = amountInSats.toBtc(),
            message = zapRequest.content,
        )
    }

fun NostrEvent.extractZapRequestOrNull() = tags.findFirstZapRequest().decodeFromJsonStringOrNull<NostrEvent>()
