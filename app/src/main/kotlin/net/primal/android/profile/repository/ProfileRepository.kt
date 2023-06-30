package net.primal.android.profile.repository

import net.primal.android.db.PrimalDatabase
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val database: PrimalDatabase,
) {
    fun observeProfile(profileId: String) = database.profiles().observeProfile(profileId = profileId)

}
