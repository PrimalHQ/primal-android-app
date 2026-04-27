package net.primal.android.main.explore.section

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.picker.BasePickerListItem
import net.primal.android.theme.AppTheme

@Composable
fun ExploreSectionListOverlayContent(
    activeSection: ExploreSection,
    onSectionClick: (ExploreSection) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppTheme.extraColorScheme.surfaceVariantAlt2)
            .padding(top = 16.dp)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(vertical = 1.dp),
    ) {
        items(items = ExploreSection.entries, key = { it.name }) { section ->
            val interactionSource = remember { MutableInteractionSource() }
            BasePickerListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(AppTheme.shapes.large)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = { onSectionClick(section) },
                    ),
                title = section.toTitle(),
                subtitle = section.toSubtitle(),
                selected = section == activeSection,
            )
        }
    }
}
