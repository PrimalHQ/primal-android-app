package net.primal.android.user.api

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.api.model.UserProfileResponse
import net.primal.android.user.api.model.UserProfilesResponse
import net.primal.android.user.domain.Relay

interface UsersApi {

    suspend fun getUserProfile(pubkey: String): UserProfileResponse

    suspend fun getUserContacts(pubkey: String, extendedResponse: Boolean = true): UserContactsResponse

    suspend fun setUserContacts(
        ownerId: String,
        contacts: Set<String>,
        relays: List<Relay>,
    ): NostrEvent

    suspend fun setUserProfile(ownerId: String, contentMetadata: ContentMetadata): NostrEvent

    suspend fun getUserProfilesMetadata(userIds: Set<String>): UserProfilesResponse
}
