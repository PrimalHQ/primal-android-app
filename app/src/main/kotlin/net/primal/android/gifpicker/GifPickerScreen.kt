package net.primal.android.gifpicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalAsyncImage
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.gifpicker.GifPickerContract.UiEvent
import net.primal.android.gifpicker.domain.GifCategory
import net.primal.android.gifpicker.domain.GifItem
import net.primal.android.gifpicker.domain.toDisplayName
import net.primal.android.theme.AppTheme

@Composable
fun GifPickerScreen(viewModel: GifPickerViewModel, callbacks: GifPickerContract.ScreenCallbacks) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, callbacks) {
        viewModel.effect.collect {
            when (it) {
                is GifPickerContract.SideEffect.GifSelected -> callbacks.onGifSelected(it.url)
            }
        }
    }

    GifPickerScreen(
        state = uiState.value,
        callbacks = callbacks,
        eventPublisher = viewModel::setEvent,
    )
}

@Composable
fun GifPickerScreen(
    state: GifPickerContract.UiState,
    callbacks: GifPickerContract.ScreenCallbacks,
    eventPublisher: (UiEvent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { eventPublisher(UiEvent.DismissError) },
    )

    PrimalScaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding(),
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 4.dp, top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GifSearchBar(
                        modifier = Modifier.weight(1f),
                        query = state.searchQuery,
                        onQueryChange = { eventPublisher(UiEvent.UpdateSearchQuery(it)) },
                    )
                    TextButton(onClick = callbacks.onClose) {
                        Text(
                            modifier = Modifier.padding(top = 1.dp),
                            text = stringResource(id = R.string.gif_picker_cancel),
                            color = AppTheme.colorScheme.onSurface,
                        )
                    }
                }

                GifCategoryChips(
                    categories = state.categories,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { eventPublisher(UiEvent.SelectCategory(it)) },
                )

                GifGridContent(
                    state = state,
                    eventPublisher = eventPublisher,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(id = R.string.gif_picker_powered_by))
                        append(" ")
                        withStyle(SpanStyle(color = AppTheme.extraColorScheme.onSurfaceVariantAlt2)) {
                            append(stringResource(id = R.string.gif_picker_klipy))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                )
            }
        },
    )
}

@Composable
private fun GifSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier.height(48.dp),
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = stringResource(id = R.string.gif_picker_search_placeholder),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = PrimalIcons.Search,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            unfocusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        ),
        shape = AppTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        singleLine = true,
        textStyle = AppTheme.typography.bodyMedium,
    )
}

@Composable
private fun GifCategoryChips(
    categories: List<GifCategory>,
    selectedCategory: GifCategory?,
    onCategorySelected: (GifCategory) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.toDisplayName(),
                        style = AppTheme.typography.bodyMedium,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    labelColor = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    selectedContainerColor = AppTheme.colorScheme.onSurface,
                    selectedLabelColor = AppTheme.colorScheme.surface,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    selectedBorderColor = AppTheme.colorScheme.onSurface,
                    enabled = true,
                    selected = isSelected,
                ),
                shape = RoundedCornerShape(20.dp),
            )
        }
    }
}

@Composable
private fun GifGridContent(
    state: GifPickerContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        val gridState = rememberLazyGridState()

        LazyVerticalGrid(
            columns = GridCells.Fixed(GIF_GRID_COLUMN_COUNT),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(state.gifItems, key = { it.id }) { gif ->
                GifGridItem(
                    gif = gif,
                    onClick = { eventPublisher(UiEvent.SelectGif(gif)) },
                )
            }
        }

        val shouldLoadMore by remember {
            derivedStateOf {
                val layoutInfo = gridState.layoutInfo
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                val totalItems = layoutInfo.totalItemsCount
                totalItems > 0 && lastVisibleIndex >= totalItems - LOAD_MORE_THRESHOLD
            }
        }

        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) {
                eventPublisher(UiEvent.LoadMoreGifs)
            }
        }

        if (state.searching && state.gifItems.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AppTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun GifGridItem(gif: GifItem, onClick: () -> Unit) {
    PrimalAsyncImage(
        model = gif.previewUrl,
        contentDescription = gif.contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
    )
}

private const val GIF_GRID_COLUMN_COUNT = 3
private const val LOAD_MORE_THRESHOLD = 6
