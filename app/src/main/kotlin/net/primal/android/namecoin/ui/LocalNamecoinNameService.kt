package net.primal.android.namecoin.ui

import androidx.compose.runtime.staticCompositionLocalOf
import net.primal.android.namecoin.NamecoinNameService

/**
 * CompositionLocal providing [NamecoinNameService] throughout the composition tree.
 * Must be provided at the application/activity level.
 */
val LocalNamecoinNameService = staticCompositionLocalOf<NamecoinNameService?> { null }
