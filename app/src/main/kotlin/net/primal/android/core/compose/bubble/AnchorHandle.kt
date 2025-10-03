package net.primal.android.core.compose.bubble

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

@Stable
class AnchorHandle {
    var rectInRoot by mutableStateOf<Rect?>(null)
    var rectInParent by mutableStateOf<Rect?>(null)
    var anchorSize by mutableStateOf<IntSize?>(null)
}
