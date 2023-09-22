package net.primal.android.core.compose.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import net.primal.android.LocalPrimalTheme

@ReadOnlyComposable
@Composable
fun isAppInDarkPrimalTheme(): Boolean = LocalPrimalTheme.current.isDarkTheme
