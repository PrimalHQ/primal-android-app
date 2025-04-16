package net.primal.android.networking.upload

import javax.inject.Inject
import net.primal.android.user.repository.BlossomRepository
import net.primal.core.networking.blossom.BlossomServerListProvider

class AndroidBlossomServerListProvider @Inject constructor(
    private val blossomRepository: BlossomRepository,
) : BlossomServerListProvider {

    override suspend fun provideBlossomServerList(userId: String): List<String> {
        return blossomRepository.ensureBlossomServerList(userId)
    }
}
