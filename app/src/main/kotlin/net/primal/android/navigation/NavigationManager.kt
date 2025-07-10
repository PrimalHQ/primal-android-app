package net.primal.android.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import net.primal.android.navigation.interactions.ArticleInteractionCallbacks
import net.primal.android.navigation.interactions.ContentInteractionCallbacks
import net.primal.android.navigation.interactions.NoteInteractionCallbacks
import net.primal.android.navigation.interactions.PrimalSubscriptionsInteractionCallbacks

val LocalNavigationManager = staticCompositionLocalOf<NavigationManager> {
    error("No NavigationManager provided. Make sure to provide it at the root of the navigation graph.")
}

data class NavigationManager(
    val noteCallbacks: NoteInteractionCallbacks,
    val articleCallbacks: ArticleInteractionCallbacks,
    val contentCallbacks: ContentInteractionCallbacks,
    val subscriptionCallbacks: PrimalSubscriptionsInteractionCallbacks,
)
