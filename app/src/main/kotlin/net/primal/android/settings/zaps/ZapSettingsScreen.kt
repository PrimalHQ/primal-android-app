package net.primal.android.settings.zaps

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.isDigitsOnly
import androidx.emoji2.emojipicker.EmojiPickerView
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SignatureErrorColumn
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.FeedZaps
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme
import net.primal.domain.notifications.ContentZapConfigItem
import net.primal.domain.notifications.ContentZapDefault
import net.primal.domain.notifications.DEFAULT_ZAP_CONFIG
import net.primal.domain.notifications.DEFAULT_ZAP_DEFAULT

@Composable
fun ZapSettingsScreen(viewModel: ZapSettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()
    ZapSettingsScreen(
        uiState = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZapSettingsScreen(
    uiState: ZapSettingsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (ZapSettingsContract.UiEvent) -> Unit,
) {
    val backSequence = {
        if (uiState.editPresetIndex == null) {
            onClose()
        } else {
            eventPublisher(ZapSettingsContract.UiEvent.CloseEditor)
        }
    }

    BackHandler(enabled = uiState.editPresetIndex != null) {
        backSequence()
    }

    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_zaps_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = backSequence,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
            )
        },
        content = { paddingValues ->
            SignatureErrorColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues,
                signatureUiError = uiState.signatureError,
            ) {
                ZapSettingsAnimatedContent(
                    uiState = uiState,
                    contentPadding = paddingValues,
                    eventPublisher = eventPublisher,
                )
            }
        },
    )
}

@Composable
private fun ZapSettingsAnimatedContent(
    uiState: ZapSettingsContract.UiState,
    contentPadding: PaddingValues,
    eventPublisher: (ZapSettingsContract.UiEvent) -> Unit,
) {
    AnimatedContent(
        modifier = Modifier
            .background(color = AppTheme.colorScheme.surfaceVariant)
            .fillMaxSize(),
        targetState = uiState.editPresetIndex,
        transitionSpec = {
            when (targetState) {
                null -> {
                    slideInHorizontally(initialOffsetX = { -it })
                        .togetherWith(
                            slideOutHorizontally(targetOffsetX = { it }),
                        )
                }

                else -> {
                    slideInHorizontally(initialOffsetX = { it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
                }
            }
        },
        label = "ZapSettings",
    ) {
        when (it) {
            null -> {
                ZapPresetsList(
                    paddingValues = contentPadding,
                    zapDefault = uiState.zapDefault,
                    zapsConfig = uiState.zapConfig,
                    onZapDefaultClick = {
                        eventPublisher(ZapSettingsContract.UiEvent.EditZapDefault)
                    },
                    onPresetClick = { presetItem ->
                        eventPublisher(ZapSettingsContract.UiEvent.EditZapPreset(presetItem))
                    },
                )
            }

            -1 -> {
                ZapDefaultEditor(
                    paddingValues = contentPadding,
                    zapDefault = uiState.zapDefault!!,
                    onUpdate = {
                        eventPublisher(ZapSettingsContract.UiEvent.UpdateZapDefault(it))
                    },
                    updating = uiState.saving,
                )
            }

            in (0..PRESETS_COUNT) -> {
                ZapPresetEditor(
                    paddingValues = contentPadding,
                    index = it,
                    zapsConfig = uiState.zapConfig,
                    updating = uiState.saving,
                    onUpdate = { zapPreset ->
                        eventPublisher(
                            ZapSettingsContract.UiEvent.UpdateZapPreset(
                                index = it,
                                zapPreset = zapPreset,
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun ZapPresetsList(
    paddingValues: PaddingValues,
    zapDefault: ContentZapDefault?,
    zapsConfig: List<ContentZapConfigItem> = emptyList(),
    onZapDefaultClick: () -> Unit,
    onPresetClick: (ContentZapConfigItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (zapDefault != null) {
            item {
                ZapDefaultListItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable {
                            onZapDefaultClick()
                        },
                    text = zapDefault.message,
                    amount = zapDefault.amount,
                )

                Text(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    text = stringResource(id = R.string.settings_zaps_description),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                )
            }
        }

        if (zapsConfig.isNotEmpty()) {
            item {
                ZapCustomPresets(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    presets = zapsConfig,
                    onPresetClick = {
                        onPresetClick(it)
                    },
                )
            }
        }
    }
}

@Composable
private fun ZapDefaultListItem(
    modifier: Modifier,
    text: String,
    amount: Long,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    Card(modifier = modifier) {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
            ),
            leadingContent = {
                Icon(imageVector = PrimalIcons.FeedZaps, contentDescription = null)
            },
            headlineContent = {
                Text(
                    text = text,
                    color = AppTheme.colorScheme.onPrimary,
                )
            },
            supportingContent = {
                Text(
                    modifier = Modifier.padding(vertical = 2.dp),
                    text = "${numberFormat.format(amount)} sats",
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                )
            },
            trailingContent = {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null)
            },
        )
    }
}

@Composable
private fun ZapCustomPresets(
    modifier: Modifier,
    presets: List<ContentZapConfigItem>,
    onPresetClick: (ContentZapConfigItem) -> Unit,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    Card(modifier = modifier) {
        presets.forEach { zapItem ->
            Column {
                ListItem(
                    modifier = Modifier.clickable {
                        onPresetClick(zapItem)
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
                    ),
                    leadingContent = {
                        Text(text = zapItem.emoji)
                    },
                    headlineContent = {
                        Text(
                            text = zapItem.message,
                            color = AppTheme.colorScheme.onPrimary,
                        )
                    },
                    supportingContent = {
                        Text(
                            modifier = Modifier.padding(vertical = 2.dp),
                            text = "${numberFormat.format(zapItem.amount)} sats",
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        )
                    },
                    trailingContent = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null)
                    },
                )
                PrimalDivider()
            }
        }
    }
}

@Composable
fun ZapPresetEditor(
    paddingValues: PaddingValues,
    updating: Boolean,
    index: Int,
    zapsConfig: List<ContentZapConfigItem>,
    onUpdate: (ContentZapConfigItem) -> Unit,
) {
    val zapConfig = zapsConfig[index]

    var emoji by remember { mutableStateOf(zapConfig.emoji) }
    var message by remember { mutableStateOf(zapConfig.message) }
    var amount by remember { mutableStateOf(zapConfig.amount.toString()) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(paddingValues),
    ) {
        ZapPresetForm(
            emojiValue = emoji,
            onEmojiValueChange = {
                emoji = it
            },
            messageValue = message,
            onMessageValueChanged = {
                message = it
            },
            amountValue = amount,
            onAmountValueChanged = {
                when {
                    it.isEmpty() -> amount = ""
                    it.isDigitsOnly() && it.length <= 8 && it.toLong() > 0 -> amount = it
                }
            },
        )

        Spacer(modifier = Modifier.height(24.dp))

        val keyboard = LocalSoftwareKeyboardController.current
        PrimalLoadingButton(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
            enabled = emoji.isEmoji() && message.isNotEmpty() && amount.toLongOrNull() != null,
            loading = updating,
            text = stringResource(id = R.string.settings_zaps_editor_save),
            onClick = {
                keyboard?.hide()
                onUpdate(
                    ContentZapConfigItem(
                        emoji = emoji,
                        message = message,
                        amount = amount.toLong(),
                    ),
                )
            },
        )
    }
}

@Composable
private fun ZapPresetForm(
    messageValue: String,
    onMessageValueChanged: (String) -> Unit,
    amountValue: String,
    onAmountValueChanged: (String) -> Unit,
    emojiValue: String? = null,
    onEmojiValueChange: ((String) -> Unit)? = null,
) {
    if (emojiValue != null && onEmojiValueChange != null) {
        var emojiPickerVisible by remember { mutableStateOf(false) }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = stringResource(id = R.string.settings_zaps_editor_emoji).uppercase(),
            style = AppTheme.typography.bodySmall,
        )

        Box(
            modifier = Modifier
                .width(96.dp)
                .height(56.dp)
                .padding(horizontal = 16.dp)
                .background(color = AppTheme.extraColorScheme.surfaceVariantAlt3, shape = AppTheme.shapes.medium)
                .clickable {
                    emojiPickerVisible = true
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = emojiValue,
                style = AppTheme.typography.bodyMedium.copy(fontSize = 24.sp),
            )
        }

        if (emojiPickerVisible) {
            EmojiPicker(
                onEmojiSelected = {
                    onEmojiValueChange(it)
                },
                onDismissRequest = {
                    emojiPickerVisible = false
                },
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        text = stringResource(id = R.string.settings_zaps_editor_message).uppercase(),
        style = AppTheme.typography.bodySmall,
    )

    OutlinedTextField(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        colors = PrimalDefaults.outlinedTextFieldColors(),
        shape = AppTheme.shapes.medium,
        value = messageValue,
        onValueChange = onMessageValueChanged,
        textStyle = AppTheme.typography.bodyMedium,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        text = stringResource(id = R.string.settings_zaps_editor_value).uppercase(),
        style = AppTheme.typography.bodySmall,
    )

    OutlinedTextField(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        colors = PrimalDefaults.outlinedTextFieldColors(),
        shape = AppTheme.shapes.medium,
        singleLine = true,
        value = amountValue,
        onValueChange = onAmountValueChanged,
        textStyle = AppTheme.typography.bodyMedium,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
        ),
    )
}

private fun String.isEmoji(): Boolean = this.isNotEmpty()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ZapDefaultEditor(
    paddingValues: PaddingValues,
    zapDefault: ContentZapDefault,
    onUpdate: (ContentZapDefault) -> Unit,
    updating: Boolean,
) {
    var message by remember { mutableStateOf(zapDefault.message) }
    var amount by remember { mutableStateOf(zapDefault.amount.toString()) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(paddingValues),
    ) {
        ZapPresetForm(
            messageValue = message,
            onMessageValueChanged = {
                message = it
            },
            amountValue = amount,
            onAmountValueChanged = {
                when {
                    it.isEmpty() -> amount = ""
                    it.isDigitsOnly() && it.length <= 8 && it.toLong() > 0 -> amount = it
                }
            },
        )

        Spacer(modifier = Modifier.height(24.dp))

        val keyboard = LocalSoftwareKeyboardController.current
        PrimalLoadingButton(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
            enabled = message.isNotEmpty() && amount.toLongOrNull() != null,
            loading = updating,
            text = stringResource(id = R.string.settings_zaps_editor_save),
            onClick = {
                keyboard?.hide()
                onUpdate(
                    ContentZapDefault(amount = amount.toLong(), message = message),
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPicker(onEmojiSelected: (String) -> Unit, onDismissRequest: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState())
                .offset(y = (-32).dp),
            factory = {
                EmojiPickerView(it)
                    .apply {
                        setOnEmojiPickedListener { item ->
                            onEmojiSelected(item.emoji)
                            onDismissRequest()
                        }
                    }
            },
            update = {},
        )
    }
}

@Preview
@Composable
private fun ZapSettingsPreview() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        ZapSettingsScreen(
            uiState = ZapSettingsContract.UiState(
                editPresetIndex = 0,
                zapDefault = DEFAULT_ZAP_DEFAULT,
                zapConfig = DEFAULT_ZAP_CONFIG,
            ),
            onClose = {},
            eventPublisher = {},
        )
    }
}
