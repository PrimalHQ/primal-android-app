package net.primal.android.nostr.ext

import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.profile.db.ProfileData
import net.primal.android.stats.db.EventZap
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.utils.LnInvoiceUtils

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
            zapReceiverId = receiverId,
            zapRequestAt = zapRequest.createdAt,
            zapReceiptAt = zapReceipt.createdAt,
            amountInBtc = amountInSats.toBtc(),
            message = zapRequest.content,
        )
    }

private fun NostrEvent.extractZapRequestOrNull() =
    NostrJson.decodeFromStringOrNull<NostrEvent>(tags.findFirstZapRequest())
