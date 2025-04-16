package net.primal.android.settings.muted.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.settings.muted.MutedSettingsContract
import net.primal.android.settings.muted.ui.MutedSettingsBottomSection
import net.primal.android.theme.AppTheme

@Composable
fun MuteWords(
    state: MutedSettingsContract.UiState,
    eventPublisher: (MutedSettingsContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    var newMuteWord by remember { mutableStateOf("") }

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
                items = state.mutedWords,
                key = { it },
                contentType = { "MutedWord" },
            ) { mutedWord ->
                MutedWordListItem(
                    item = mutedWord,
                    onUnmuteClick = {
                        eventPublisher(
                            MutedSettingsContract.UiEvent.UnmuteWord(mutedWord),
                        )
                    },
                )
                PrimalDivider()
            }

            if (state.mutedWords.isEmpty()) {
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
            value = newMuteWord,
            onValueChange = { newMuteWord = it },
            sending = false,
            onMute = {
                eventPublisher(
                    MutedSettingsContract.UiEvent.MuteWord(newMuteWord),
                )
                newMuteWord = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            sendEnabled = newMuteWord.isNotBlank(),
            textFieldPlaceholder = stringResource(
                id = R.string.settings_muted_words_mute_new_word,
            ),
        )
    }
}

@Composable
fun MutedWordListItem(item: String, onUnmuteClick: (String) -> Unit) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        headlineContent = {
            Text(
                text = item,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.headlineLarge,
                fontSize = 14.sp,
            )
        },
        trailingContent = {
            PrimalFilledButton(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(36.dp),
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                contentColor = AppTheme.colorScheme.onSurface,
                textStyle = AppTheme.typography.titleMedium.copy(
                    lineHeight = 18.sp,
                ),
                onClick = { onUnmuteClick(item) },
            ) {
                Text(
                    text = stringResource(
                        id = R.string.settings_muted_accounts_unmute_button,
                    ).lowercase(),
                )
            }
        },
    )
}
