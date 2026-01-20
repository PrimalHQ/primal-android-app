package net.primal.data.account.remote.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.account.remote.blossom.BlossomsApi
import net.primal.data.account.remote.blossom.BlossomsApiImpl
import net.primal.data.account.remote.pushnotifications.PushNotificationApi
import net.primal.data.account.remote.pushnotifications.PushNotificationApiImpl

object AccountApiServiceFactory {
    fun createBlossomsApi(primalApiClient: PrimalApiClient): BlossomsApi =
        BlossomsApiImpl(primalApiClient = primalApiClient)

    fun createPushNotificationApi(primalApiClient: PrimalApiClient): PushNotificationApi =
        PushNotificationApiImpl(primalApiClient = primalApiClient)
}
