package net.primal.android.premium.manage.content.api

import net.primal.android.premium.manage.content.api.model.BroadcastingStatus

interface BroadcastApi {

    suspend fun getContentStats(userId: String): Map<Int, Long>

    suspend fun startContentRebroadcast(userId: String, kinds: List<Int>?)

    suspend fun cancelContentRebroadcast(userId: String)

    suspend fun getContentRebroadcastStatus(userId: String) : BroadcastingStatus
}
