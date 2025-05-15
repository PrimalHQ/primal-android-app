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
fun MuteWords(
    newMutedWord: String,
    mutedWords: List<String>,
    eventPublisher: (MutedSettingsContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
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
                items = mutedWords,
                key = { it },
                contentType = { "MutedWord" },
            ) { mutedWord ->
                MutedListItem(
                    item = mutedWord,
                    onUnmuteClick = {
                        eventPublisher(
                            MutedSettingsContract.UiEvent.UnmuteWord(mutedWord),
                        )
                    },
                )
                PrimalDivider()
            }

            if (mutedWords.isEmpty()) {
                item(contentType = "NoContent") {
                    ListNoContent(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(top = 48.dp),
                        noContentText = stringResource(
                            id = R.string.settings_muted_words_no_content,
                        ),
                        refreshButtonVisible = false,
                    )
                }
            }
        }

        MutedSettingsBottomSection(
            value = newMutedWord,
            onValueChange = {
                eventPublisher(
                    MutedSettingsContract.UiEvent.UpdateNewMutedWord(it),
                )
            },
            sending = false,
            onMute = {
                eventPublisher(
                    MutedSettingsContract.UiEvent.MuteWord(newMutedWord),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            sendEnabled = newMutedWord.isNotBlank(),
            textFieldPlaceholder = stringResource(
                id = R.string.settings_muted_words_mute_new_word,
            ),
        )
    }
}
