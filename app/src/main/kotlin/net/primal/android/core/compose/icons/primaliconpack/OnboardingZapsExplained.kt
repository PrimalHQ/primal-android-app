package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.OnboardingZapsExplained: ImageVector
    get() {
        if (_OnboardingZapsExplained != null) {
            return _OnboardingZapsExplained!!
        }
        _OnboardingZapsExplained = ImageVector.Builder(
            name = "OnboardingZapsExplained",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(50f, 100f)
                curveTo(77.61f, 100f, 100f, 77.61f, 100f, 50f)
                curveTo(100f, 22.39f, 77.61f, 0f, 50f, 0f)
                curveTo(22.39f, 0f, 0f, 22.39f, 0f, 50f)
                curveTo(0f, 77.61f, 22.39f, 100f, 50f, 100f)
                close()
                moveTo(69.58f, 42.24f)
                curveTo(70.6f, 42.24f, 71.19f, 43.35f, 70.6f, 44.15f)
                lineTo(46.4f, 80.23f)
                curveTo(44.86f, 82.32f, 41.45f, 80.94f, 41.92f, 78.41f)
                lineTo(44.96f, 57.8f)
                horizontalLineTo(30.42f)
                curveTo(29.39f, 57.8f, 28.8f, 56.66f, 29.42f, 55.87f)
                lineTo(53.43f, 21.79f)
                curveTo(55.03f, 19.76f, 58.38f, 21.21f, 57.87f, 23.71f)
                lineTo(54.95f, 42.24f)
                horizontalLineTo(69.58f)
                close()
            }
        }.build()

        return _OnboardingZapsExplained!!
    }

@Suppress("ObjectPropertyName")
private var _OnboardingZapsExplained: ImageVector? = null
