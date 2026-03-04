package net.primal.android.core.di

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNoticePreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    fun isNoticeDismissed(userId: String, noticeKey: String): Boolean =
        prefs.getBoolean("${userId}_${noticeKey}_dismissed", false)

    fun setNoticeDismissed(userId: String, noticeKey: String) {
        prefs.edit(commit = true) {
            putBoolean("${userId}_${noticeKey}_dismissed", true)
        }
    }

    companion object {
        private const val PREFS_NAME = "app_notices"

        const val NOTICE_WALLET_DISCONTINUED = "wallet_discontinued"
        const val NOTICE_WALLET_DETECTED = "wallet_detected"
    }
}
