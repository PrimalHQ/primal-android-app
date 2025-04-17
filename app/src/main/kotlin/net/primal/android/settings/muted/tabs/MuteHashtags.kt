package net.primal.android.settings.muted.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.settings.muted.MutedSettingsContract
import net.primal.android.settings.muted.ui.MutedListItem
import net.primal.android.settings.muted.ui.MutedSettingsBottomSection

@Composable
fun MuteHashtags(
    mutedHashtags: List<String>,
    eventPublisher: (MutedSettingsContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    var newMutedHashtag by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding(),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            items(
                items = mutedHashtags,
                key = { it },
                contentType = { "MutedHashtag" },
            ) { mutedWord ->
                MutedListItem(
                    item = "#$mutedWord",
                    onUnmuteClick = {
                        eventPublisher(
                            MutedSettingsContract.UiEvent.UnmuteHashtag(mutedWord),
                        )
                    },
                )
                PrimalDivider()
            }

            if (mutedHashtags.isEmpty()) {
                item(contentType = "NoContent") {
                    ListNoContent(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(top = 48.dp),
                        noContentText = stringResource(
                            id = R.string.settings_muted_hashtags_no_content,
                        ),
                        refreshButtonVisible = false,
                    )
                }
            }
        }

        MutedSettingsBottomSection(
            value = newMutedHashtag,
            onValueChange = { newMutedHashtag = it },
            sending = false,
            onMute = {
                eventPublisher(
                    MutedSettingsContract.UiEvent.MuteHashtag(newMutedHashtag),
                )
                newMutedHashtag = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            sendEnabled = newMutedHashtag.isNotBlank(),
            textFieldPlaceholder = stringResource(
                id = R.string.settings_muted_hashtags_mute_new_hashtag,
            ),
            showLeadingHashtag = true,
        )
    }
}
