package net.primal.android.user.api

import net.primal.android.user.api.model.UserContactsResponse
import net.primal.android.user.api.model.UserProfileResponse

interface UsersApi {

    suspend fun getUserProfile(pubkey: String): UserProfileResponse

    suspend fun getUserContacts(pubkey: String): UserContactsResponse

}
