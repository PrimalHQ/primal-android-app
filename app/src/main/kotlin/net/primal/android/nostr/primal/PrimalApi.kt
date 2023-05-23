package net.primal.android.nostr.primal

import kotlinx.coroutines.Job

interface PrimalApi {

    fun requestDefaultAppSettings(): Job

    fun requestFeedUpdates(feedHex: String, userHex: String): Job

    fun searchContent(query: String): Job
}