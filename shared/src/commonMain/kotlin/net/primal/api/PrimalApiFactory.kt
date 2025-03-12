package net.primal.api

import net.primal.PrimalLib
import net.primal.api.feed.FeedApi

object PrimalApiFactory {

    fun createFeedsApi(): FeedApi = PrimalLib.getKoin().get()
}
