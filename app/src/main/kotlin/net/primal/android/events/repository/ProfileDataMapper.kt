package net.primal.android.events.repository

import net.primal.android.profile.db.ProfileData as ProfileDataPO
import net.primal.domain.model.ProfileData as ProfileDataDO

internal fun ProfileDataPO.asProfileDataDO(): ProfileDataDO {
    return ProfileDataDO(
        profileId = this.ownerId,
        metadataEventId = this.eventId,
        createdAt = this.createdAt,
        metadataRawEvent = this.raw,
        handle = this.handle,
        displayName = this.displayName,
        internetIdentifier = this.internetIdentifier,
        lightningAddress = this.lightningAddress,
        lnUrlDecoded = this.lnUrlDecoded,
        avatarCdnImage = this.avatarCdnImage,
        bannerCdnImage = this.bannerCdnImage,
        website = this.website,
        about = this.about,
        aboutUris = this.aboutUris,
        aboutHashtags = this.aboutHashtags,
        primalName = this.primalName,
        primalPremiumInfo = this.primalPremiumInfo,
        blossoms = this.blossoms,
    )
}
