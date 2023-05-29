package net.primal.android.nostr.primal

import kotlinx.coroutines.Job

interface PrimalApi {

    fun requestDefaultAppSettings(): Job

    fun requestFeedUpdates(feedDirective: String, userPubkey: String): Job

    fun searchContent(query: String): Job
}