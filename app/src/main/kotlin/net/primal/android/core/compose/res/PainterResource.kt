package net.primal.android.core.compose.res

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme

@Composable
fun painterResource(@DrawableRes lightResId: Int, @DrawableRes darkResId: Int) =
    painterResource(id = if (isAppInDarkPrimalTheme()) darkResId else lightResId)
