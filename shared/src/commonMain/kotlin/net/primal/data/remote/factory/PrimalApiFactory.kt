package net.primal.data.remote.factory

import net.primal.PrimalLib
import net.primal.data.remote.api.feed.FeedApi

object PrimalApiFactory {

    fun createFeedsApi(): FeedApi = PrimalLib.getKoin().get()
}
