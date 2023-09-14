package net.primal.android.core.compose.res

import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

@Composable
fun painterResource(@DrawableRes lightResId: Int, @DrawableRes darkResId: Int) =
    painterResource(id = if (isSystemInDarkTheme()) darkResId else lightResId)
