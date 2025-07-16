package net.primal.android.navigation.navigator

import net.primal.android.navigation.interactions.ArticleInteractionCallbacks
import net.primal.android.navigation.interactions.ContentInteractionCallbacks
import net.primal.android.navigation.interactions.NoteInteractionCallbacks
import net.primal.android.navigation.interactions.PrimalSubscriptionsInteractionCallbacks

interface PrimalNavigator :
    NoteInteractionCallbacks,
    ArticleInteractionCallbacks,
    ContentInteractionCallbacks,
    PrimalSubscriptionsInteractionCallbacks
