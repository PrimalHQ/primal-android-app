package net.primal.android.core.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun getListItemShape(index: Int, listSize: Int): Shape {
    val isFirst = index == 0
    val isLast = index == listSize - 1
    return when {
        isFirst && isLast -> RoundedCornerShape(size = 12.dp)
        isFirst -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        isLast -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
        else -> RectangleShape
    }
}
