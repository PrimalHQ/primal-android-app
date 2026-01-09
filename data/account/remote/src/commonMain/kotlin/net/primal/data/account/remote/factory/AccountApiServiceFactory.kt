package net.primal.data.account.remote.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.account.remote.blossom.BlossomsApi
import net.primal.data.account.remote.blossom.BlossomsApiImpl

object AccountApiServiceFactory {
    fun createBlossomsApi(primalApiClient: PrimalApiClient): BlossomsApi =
        BlossomsApiImpl(primalApiClient = primalApiClient)
}
