package net.primal.android.explore.home.topics.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.InfiniteLottieAnimation

@Composable
fun TopicLoadingPlaceholder(
    repeat: Int,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val animationRawResId = when (LocalPrimalTheme.current.isDarkTheme) {
        true -> R.raw.primal_loader_generic_square_dark
        false -> R.raw.primal_loader_generic_square_light
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
            .padding(contentPadding),
    ) {
        repeat(times = repeat) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .padding(horizontal = 16.dp),
            ) {
                KindOneRow(resId = animationRawResId)
                KindTwoRow(resId = animationRawResId)
                KindThreeRow(resId = animationRawResId)
                KindFourRow(resId = animationRawResId)
                KindFiveRow(resId = animationRawResId)
            }
        }
    }
}

@Composable
private fun KindOneRow(resId: Int) {
    UniversalRow {
        TopicLoadingItem(
            resId = resId,
            weight = 1f,
        )
        TopicLoadingItem(
            resId = resId,
            weight = 2f,
        )
        TopicLoadingItem(
            resId = resId,
            weight = 1f,
        )
    }
}

@Composable
private fun KindTwoRow(resId: Int) {
    UniversalRow(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        TopicLoadingItem(
            resId = resId,
            weight = 1f,
        )
        TopicLoadingItem(
            resId = resId,
            weight = 1f,
        )
    }
}

@Composable
private fun KindThreeRow(resId: Int) {
    UniversalRow(
        modifier = Modifier.padding(horizontal = 28.dp),
    ) {
        TopicLoadingItem(
            resId = resId,
            weight = 1f,
        )
        TopicLoadingItem(
            resId = resId,
            weight = 1f,
        )
        TopicLoadingItem(
            resId = resId,
            weight = 1f,
        )
    }
}

@Composable
private fun KindFourRow(resId: Int) {
    UniversalRow {
        TopicLoadingItem(
            resId = resId,
            weight = 2f,
        )
        TopicLoadingItem(
            resId = resId,
            weight = 1f,
        )
        TopicLoadingItem(
            resId = resId,
            weight = 1f,
        )
    }
}

@Composable
private fun KindFiveRow(resId: Int) {
    UniversalRow(
        modifier = Modifier.padding(horizontal = 36.dp),
    ) {
        TopicLoadingItem(
            resId = resId,
            weight = 1.5f,
        )
        TopicLoadingItem(
            resId = resId,
            weight = 2f,
        )
    }
}

@Composable
private fun UniversalRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
        content()
    }
}

@Composable
private fun RowScope.TopicLoadingItem(
    resId: Int,
    weight: Float,
    modifier: Modifier = Modifier,
) {
    InfiniteLottieAnimation(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(percent = 100))
            .fillMaxWidth()
            .scale(20f)
            .weight(weight = weight)
            .border(width = 1.dp, color = Color.Red),
        resId = resId,
    )
}
