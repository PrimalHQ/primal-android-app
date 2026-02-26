package net.primal.android.gifpicker.domain

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.primal.android.R

enum class GifCategory(val displayName: String) {
    TRENDING("Trending"),
    YES("Yes"),
    NO("No"),
    OMG("OMG"),
    FUNNY("Funny"),
    SAD("Sad"),
    LOVE("Love"),
    ANGRY("Angry"),
    DUMB("Dumb"),
    TERRIBLE("Terrible"),
    WOW("Wow"),
    CRINGE("Cringe"),
    LOL("LOL"),
    SAVAGE("Savage"),
    BLESSED("Blessed"),
}

@Suppress("CyclomaticComplexMethod")
@Composable
fun GifCategory.toDisplayName(): String =
    when (this) {
        GifCategory.TRENDING -> stringResource(id = R.string.gif_category_trending)
        GifCategory.OMG -> stringResource(id = R.string.gif_category_omg)
        GifCategory.FUNNY -> stringResource(id = R.string.gif_category_funny)
        GifCategory.SAD -> stringResource(id = R.string.gif_category_sad)
        GifCategory.LOVE -> stringResource(id = R.string.gif_category_love)
        GifCategory.ANGRY -> stringResource(id = R.string.gif_category_angry)
        GifCategory.DUMB -> stringResource(id = R.string.gif_category_dumb)
        GifCategory.TERRIBLE -> stringResource(id = R.string.gif_category_terrible)
        GifCategory.WOW -> stringResource(id = R.string.gif_category_wow)
        GifCategory.CRINGE -> stringResource(id = R.string.gif_category_cringe)
        GifCategory.YES -> stringResource(id = R.string.gif_category_yes)
        GifCategory.NO -> stringResource(id = R.string.gif_category_no)
        GifCategory.LOL -> stringResource(id = R.string.gif_category_lol)
        GifCategory.SAVAGE -> stringResource(id = R.string.gif_category_savage)
        GifCategory.BLESSED -> stringResource(id = R.string.gif_category_blessed)
    }
