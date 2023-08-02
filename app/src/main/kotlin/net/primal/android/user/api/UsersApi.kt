package net.primal.android.user.api

import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.api.model.UserProfileResponse

interface UsersApi {

    suspend fun getUserProfile(pubkey: String): UserProfileResponse

    suspend fun getUserContacts(pubkey: String): UserContactsResponse

    @Throws(WssException::class, NostrPublishException::class)
    suspend fun follow(ownerPubkey: String, followedPubkey: String, relays: List<String>): Set<String>?

    @Throws(WssException::class, NostrPublishException::class)
    suspend fun unfollow(ownerPubkey: String, unfollowedPubkey: String, relays: List<String>): Set<String>?
}
