package net.primal.api

import net.primal.PrimalLib
import net.primal.api.feeds.FeedApi

object PrimalApiFactory {

    fun createFeedsApi(): FeedApi = PrimalLib.getKoin().get()
}
