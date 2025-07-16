package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.primal.android.navigation.navigator.PrimalNavigator
import net.primal.android.nostr.mappers.asFeedPostUi
import net.primal.android.notes.feed.model.NoteNostrUriUi

@Composable
fun ReferencedNotesColumn(
    postResources: List<NoteNostrUriUi>,
    expanded: Boolean,
    containerColor: Color,
    navigator: PrimalNavigator,
    nestingLevel: Int,
    nestingCutOffLimit: Int,
    modifier: Modifier = Modifier,
    hasBorder: Boolean = false,
) {
    val displayableNotes = if (postResources.isNotEmpty()) {
        if (expanded) postResources else postResources.subList(0, 1)
    } else {
        emptyList()
    }

    Column(modifier = modifier) {
        displayableNotes.forEach { nostrResourceUi ->
            val data = nostrResourceUi.referencedNote
            checkNotNull(data)
            ReferencedNoteCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                data = data.asFeedPostUi(),
                hasBorder = hasBorder,
                nestingLevel = nestingLevel,
                nestingCutOffLimit = nestingCutOffLimit,
                colors = CardDefaults.cardColors(containerColor = containerColor),
                navigator = navigator,
            )
        }
    }
}
