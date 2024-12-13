package net.primal.android.thread.articles.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.AvatarOverlap
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.highlights.model.JoinedHighlightsUi


@Composable
fun HighlightActivityBottomSheetHandler(
    selectedHighlight: JoinedHighlightsUi?,
    dismissSelection: () -> Unit,
) {
    if (selectedHighlight != null) {
        HighlightActivityBottomSheet(
            onDismissRequest = dismissSelection,
            selectedHighlight = selectedHighlight,
        )
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightActivityBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    selectedHighlight: JoinedHighlightsUi,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        Scaffold(
            topBar = {
                PrimalTopAppBar(
                    title = "Highlight Activity",
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues),
            ) {
                HighlightAuthorsRow(authors = selectedHighlight.authors)
            }


        }
    }
}

@Composable
fun HighlightAuthorsRow(
    authors: Set<ProfileDetailsUi>,
) {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = PrimalIcons.Highlight,
        )
        AvatarThumbnailsRow(
            avatarCdnImages = authors.map { it.avatarCdnImage },
            avatarLegendaryCustomizations = authors.map { it.premiumDetails?.legendaryCustomization },
            avatarOverlap = AvatarOverlap.None,
        )
    }

}
