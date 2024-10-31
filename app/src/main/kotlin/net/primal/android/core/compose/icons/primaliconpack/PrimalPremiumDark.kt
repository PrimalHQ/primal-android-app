package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons
import kotlin.Suppress

val PrimalIcons.PrimalPremiumDark: ImageVector
    get() {
        if (_PrimalPremiumDark != null) {
            return _PrimalPremiumDark!!
        }
        _PrimalPremiumDark = ImageVector.Builder(
            name = "PrimalPremiumDark",
            defaultWidth = 188.dp,
            defaultHeight = 49.dp,
            viewportWidth = 188f,
            viewportHeight = 49f
        ).apply {
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.03f to Color(0xFFFA3C3C),
                        1f to Color(0xFFBC1870)
                    ),
                    start = Offset(14.05f, 18.64f),
                    end = Offset(14.02f, 38.64f)
                )
            ) {
                moveTo(27.34f, 44.48f)
                curveTo(25.78f, 44.82f, 24.16f, 45f, 22.5f, 45f)
                curveTo(17.94f, 45f, 13.7f, 43.65f, 10.16f, 41.32f)
                curveTo(9.28f, 40.06f, 8.89f, 39.37f, 8.6f, 38.86f)
                curveTo(8.46f, 38.61f, 8.34f, 38.41f, 8.2f, 38.2f)
                curveTo(6.86f, 36.04f, 6.16f, 33.27f, 6.01f, 30.01f)
                curveTo(5.53f, 19.92f, 11.67f, 13.47f, 17.93f, 12.42f)
                curveTo(21.91f, 11.75f, 25.06f, 12.43f, 27.48f, 13.72f)
                curveTo(25.34f, 13.12f, 22.79f, 13.09f, 19.88f, 13.92f)
                curveTo(12.83f, 16.19f, 10.48f, 23.11f, 11.49f, 30.76f)
                curveTo(13.24f, 40.35f, 22.65f, 43.79f, 27.34f, 44.48f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFF9F2F),
                        1f to Color(0xFFFA3C3C)
                    ),
                    start = Offset(10.99f, 9.14f),
                    end = Offset(9.87f, 29.15f)
                )
            ) {
                moveTo(7.25f, 39.04f)
                curveTo(5.94f, 36.71f, 4.75f, 33.24f, 4.6f, 30.08f)
                curveTo(4.1f, 19.37f, 10.65f, 12.22f, 17.7f, 11.03f)
                curveTo(27.31f, 9.41f, 32.66f, 15.1f, 34.5f, 19.32f)
                curveTo(34.58f, 19.27f, 34.61f, 19.17f, 34.57f, 19.08f)
                curveTo(31.54f, 12.49f, 25.27f, 7.97f, 18.03f, 7.97f)
                curveTo(9.82f, 7.97f, 2.48f, 13.86f, 0f, 22.63f)
                curveTo(0.04f, 29.12f, 2.82f, 34.96f, 7.25f, 39.04f)
                close()
            }
            path(
                fill = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF5B09AD),
                        1f to Color(0xFFBC1870)
                    ),
                    start = Offset(26.72f, 44.53f),
                    end = Offset(26.78f, 21.33f)
                )
            ) {
                moveTo(35.16f, 41.11f)
                curveTo(33.53f, 42.21f, 31.76f, 43.11f, 29.87f, 43.77f)
                curveTo(28.96f, 43.61f, 27.96f, 43.41f, 27.25f, 43.27f)
                curveTo(26.91f, 43.21f, 26.64f, 43.15f, 26.48f, 43.12f)
                curveTo(22.2f, 42.34f, 14.55f, 39.59f, 12.88f, 30.54f)
                curveTo(12.4f, 26.91f, 12.74f, 23.56f, 13.94f, 20.9f)
                curveTo(15.13f, 18.29f, 17.17f, 16.28f, 20.28f, 15.27f)
                curveTo(23.9f, 14.28f, 26.9f, 14.77f, 29.13f, 15.97f)
                curveTo(28.58f, 15.85f, 28.02f, 15.79f, 27.44f, 15.79f)
                curveTo(22.61f, 15.79f, 18.71f, 19.98f, 18.71f, 25.15f)
                curveTo(18.71f, 27.21f, 19.33f, 29.12f, 20.38f, 30.66f)
                curveTo(20.38f, 30.66f, 23.41f, 36.37f, 31.63f, 35.86f)
                curveTo(38.97f, 35.41f, 42.78f, 28.83f, 43.23f, 26.4f)
                curveTo(43.47f, 25.13f, 43.59f, 23.83f, 43.59f, 22.5f)
                curveTo(43.59f, 10.85f, 34.15f, 1.41f, 22.5f, 1.41f)
                curveTo(13.68f, 1.41f, 6.13f, 6.81f, 2.98f, 14.49f)
                curveTo(1.91f, 15.88f, 1f, 17.42f, 0.26f, 19.08f)
                curveTo(1.91f, 8.28f, 11.24f, 0f, 22.5f, 0f)
                curveTo(34.93f, 0f, 45f, 10.07f, 45f, 22.5f)
                curveTo(45f, 30.23f, 41.1f, 37.06f, 35.16f, 41.11f)
                close()
            }
            path(fill = SolidColor(Color(0xFFDDDDDD))) {
                moveTo(59f, 44.06f)
                horizontalLineTo(66.8f)
                verticalLineTo(32.07f)
                curveTo(67.5f, 32.86f, 68.36f, 33.5f, 69.38f, 33.99f)
                curveTo(70.42f, 34.48f, 71.72f, 34.72f, 73.27f, 34.72f)
                curveTo(75.95f, 34.72f, 78.13f, 33.75f, 79.81f, 31.82f)
                curveTo(81.49f, 29.89f, 82.33f, 27.27f, 82.33f, 23.98f)
                verticalLineTo(23.25f)
                curveTo(82.33f, 19.9f, 81.51f, 17.31f, 79.88f, 15.46f)
                curveTo(78.25f, 13.62f, 76.08f, 12.7f, 73.39f, 12.7f)
                curveTo(71.85f, 12.7f, 70.49f, 13f, 69.31f, 13.59f)
                curveTo(68.14f, 14.19f, 67.27f, 14.92f, 66.71f, 15.81f)
                horizontalLineTo(66.64f)
                verticalLineTo(13.25f)
                horizontalLineTo(59f)
                verticalLineTo(44.06f)
                close()
                moveTo(66.8f, 26.84f)
                verticalLineTo(20.65f)
                curveTo(67.06f, 19.96f, 67.51f, 19.37f, 68.15f, 18.87f)
                curveTo(68.79f, 18.35f, 69.58f, 18.09f, 70.53f, 18.09f)
                curveTo(71.84f, 18.09f, 72.81f, 18.54f, 73.45f, 19.44f)
                curveTo(74.11f, 20.32f, 74.44f, 21.53f, 74.44f, 23.07f)
                verticalLineTo(24.28f)
                curveTo(74.44f, 25.79f, 74.11f, 27.03f, 73.45f, 28f)
                curveTo(72.81f, 28.96f, 71.82f, 29.44f, 70.48f, 29.44f)
                curveTo(69.58f, 29.44f, 68.81f, 29.18f, 68.17f, 28.67f)
                curveTo(67.53f, 28.13f, 67.07f, 27.53f, 66.8f, 26.84f)
                close()
            }
            path(fill = SolidColor(Color(0xFFDDDDDD))) {
                moveTo(85.12f, 34.26f)
                horizontalLineTo(93.14f)
                verticalLineTo(21.97f)
                curveTo(93.72f, 20.92f, 94.5f, 20.09f, 95.48f, 19.46f)
                curveTo(96.45f, 18.82f, 97.44f, 18.5f, 98.43f, 18.5f)
                curveTo(98.93f, 18.5f, 99.39f, 18.53f, 99.8f, 18.59f)
                curveTo(100.21f, 18.64f, 100.56f, 18.72f, 100.83f, 18.85f)
                verticalLineTo(13.13f)
                curveTo(100.68f, 13.07f, 100.39f, 13.02f, 99.98f, 12.98f)
                curveTo(99.57f, 12.91f, 99.14f, 12.88f, 98.68f, 12.88f)
                curveTo(97.44f, 12.88f, 96.35f, 13.19f, 95.39f, 13.8f)
                curveTo(94.44f, 14.41f, 93.66f, 15.2f, 93.05f, 16.17f)
                horizontalLineTo(93.03f)
                verticalLineTo(13.25f)
                horizontalLineTo(85.12f)
                verticalLineTo(34.26f)
                close()
            }
            path(fill = SolidColor(Color(0xFFDDDDDD))) {
                moveTo(102.93f, 34.26f)
                horizontalLineTo(110.89f)
                verticalLineTo(13.25f)
                horizontalLineTo(102.93f)
                verticalLineTo(34.26f)
                close()
                moveTo(102.54f, 5.87f)
                curveTo(102.54f, 6.97f, 102.92f, 7.91f, 103.67f, 8.7f)
                curveTo(104.41f, 9.48f, 105.49f, 9.87f, 106.89f, 9.87f)
                curveTo(108.29f, 9.87f, 109.38f, 9.48f, 110.14f, 8.7f)
                curveTo(110.92f, 7.91f, 111.3f, 6.96f, 111.3f, 5.85f)
                curveTo(111.3f, 4.72f, 110.93f, 3.79f, 110.18f, 3.04f)
                curveTo(109.44f, 2.29f, 108.35f, 1.92f, 106.94f, 1.92f)
                curveTo(105.53f, 1.92f, 104.45f, 2.3f, 103.69f, 3.06f)
                curveTo(102.93f, 3.81f, 102.54f, 4.74f, 102.54f, 5.87f)
                close()
            }
            path(fill = SolidColor(Color(0xFFDDDDDD))) {
                moveTo(114.8f, 34.26f)
                horizontalLineTo(122.62f)
                verticalLineTo(21.06f)
                curveTo(122.97f, 20.31f, 123.43f, 19.7f, 123.97f, 19.21f)
                curveTo(124.54f, 18.71f, 125.21f, 18.46f, 125.99f, 18.46f)
                curveTo(126.86f, 18.46f, 127.53f, 18.69f, 128f, 19.17f)
                curveTo(128.49f, 19.64f, 128.73f, 20.47f, 128.73f, 21.65f)
                verticalLineTo(34.26f)
                horizontalLineTo(136.57f)
                verticalLineTo(21.06f)
                curveTo(136.93f, 20.3f, 137.38f, 19.68f, 137.95f, 19.19f)
                curveTo(138.51f, 18.7f, 139.18f, 18.46f, 139.96f, 18.46f)
                curveTo(140.83f, 18.46f, 141.5f, 18.69f, 141.97f, 19.17f)
                curveTo(142.45f, 19.62f, 142.68f, 20.45f, 142.68f, 21.65f)
                verticalLineTo(34.26f)
                horizontalLineTo(150.53f)
                verticalLineTo(20.33f)
                curveTo(150.53f, 17.59f, 149.89f, 15.63f, 148.63f, 14.46f)
                curveTo(147.36f, 13.27f, 145.68f, 12.68f, 143.57f, 12.68f)
                curveTo(141.91f, 12.68f, 140.41f, 13.02f, 139.07f, 13.71f)
                curveTo(137.73f, 14.38f, 136.65f, 15.3f, 135.84f, 16.49f)
                horizontalLineTo(135.8f)
                curveTo(135.39f, 15.24f, 134.62f, 14.3f, 133.51f, 13.66f)
                curveTo(132.4f, 13.02f, 131.07f, 12.7f, 129.53f, 12.7f)
                curveTo(127.94f, 12.7f, 126.57f, 13.03f, 125.39f, 13.68f)
                curveTo(124.22f, 14.34f, 123.26f, 15.23f, 122.53f, 16.36f)
                horizontalLineTo(122.46f)
                verticalLineTo(13.27f)
                horizontalLineTo(114.8f)
                verticalLineTo(34.26f)
                close()
            }
            path(fill = SolidColor(Color(0xFFDDDDDD))) {
                moveTo(153.22f, 28.23f)
                curveTo(153.22f, 30.3f, 153.87f, 31.88f, 155.17f, 32.96f)
                curveTo(156.48f, 34.03f, 158.28f, 34.56f, 160.57f, 34.56f)
                curveTo(162.49f, 34.56f, 164.08f, 34.23f, 165.35f, 33.58f)
                curveTo(166.63f, 32.91f, 167.53f, 32.16f, 168.04f, 31.34f)
                curveTo(168.1f, 31.9f, 168.2f, 32.44f, 168.32f, 32.94f)
                curveTo(168.46f, 33.44f, 168.65f, 33.88f, 168.89f, 34.26f)
                horizontalLineTo(176.6f)
                verticalLineTo(33.94f)
                curveTo(176.29f, 33.37f, 176.06f, 32.69f, 175.91f, 31.91f)
                curveTo(175.76f, 31.13f, 175.68f, 30.3f, 175.68f, 29.42f)
                verticalLineTo(20.28f)
                curveTo(175.68f, 17.62f, 174.84f, 15.69f, 173.17f, 14.51f)
                curveTo(171.49f, 13.3f, 168.94f, 12.7f, 165.53f, 12.7f)
                curveTo(162.16f, 12.7f, 159.51f, 13.32f, 157.57f, 14.55f)
                curveTo(155.65f, 15.77f, 154.69f, 17.39f, 154.69f, 19.42f)
                verticalLineTo(19.99f)
                lineTo(161.8f, 20.01f)
                verticalLineTo(19.53f)
                curveTo(161.8f, 18.74f, 162.07f, 18.13f, 162.6f, 17.7f)
                curveTo(163.15f, 17.26f, 163.93f, 17.04f, 164.96f, 17.04f)
                curveTo(165.99f, 17.04f, 166.74f, 17.29f, 167.2f, 17.79f)
                curveTo(167.65f, 18.3f, 167.88f, 19.02f, 167.88f, 19.96f)
                verticalLineTo(21.31f)
                horizontalLineTo(162.51f)
                curveTo(159.69f, 21.31f, 157.43f, 21.94f, 155.74f, 23.21f)
                curveTo(154.06f, 24.46f, 153.22f, 26.13f, 153.22f, 28.23f)
                close()
                moveTo(160.93f, 27.66f)
                curveTo(160.93f, 26.73f, 161.27f, 26.08f, 161.94f, 25.7f)
                curveTo(162.61f, 25.32f, 163.64f, 25.13f, 165.02f, 25.13f)
                horizontalLineTo(167.88f)
                verticalLineTo(26.75f)
                curveTo(167.88f, 27.66f, 167.46f, 28.48f, 166.63f, 29.22f)
                curveTo(165.8f, 29.95f, 164.88f, 30.31f, 163.86f, 30.31f)
                curveTo(162.91f, 30.31f, 162.19f, 30.09f, 161.69f, 29.65f)
                curveTo(161.18f, 29.21f, 160.93f, 28.55f, 160.93f, 27.66f)
                close()
            }
            path(fill = SolidColor(Color(0xFFDDDDDD))) {
                moveTo(179.55f, 34.26f)
                horizontalLineTo(187.44f)
                verticalLineTo(0.94f)
                horizontalLineTo(179.55f)
                verticalLineTo(34.26f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFDDDDDD)),
                fillAlpha = 0.8f,
                strokeAlpha = 0.8f
            ) {
                moveTo(138.47f, 39.24f)
                curveTo(138.94f, 39.24f, 139.32f, 39.25f, 139.75f, 39.3f)
                curveTo(140.92f, 39.44f, 141.99f, 40.08f, 141.99f, 41.76f)
                curveTo(141.99f, 43.54f, 140.92f, 44.27f, 139.75f, 44.41f)
                curveTo(139.32f, 44.46f, 138.94f, 44.47f, 138.47f, 44.47f)
                horizontalLineTo(136.84f)
                verticalLineTo(48f)
                horizontalLineTo(135.68f)
                verticalLineTo(39.24f)
                horizontalLineTo(138.47f)
                close()
                moveTo(136.84f, 43.44f)
                horizontalLineTo(138.53f)
                curveTo(139.8f, 43.44f, 140.8f, 43.26f, 140.8f, 41.83f)
                curveTo(140.8f, 40.54f, 139.87f, 40.27f, 138.53f, 40.27f)
                horizontalLineTo(136.84f)
                verticalLineTo(43.44f)
                close()
                moveTo(149.43f, 47.93f)
                verticalLineTo(48f)
                horizontalLineTo(148.06f)
                curveTo(147.93f, 47.45f, 147.79f, 46.7f, 147.7f, 45.9f)
                curveTo(147.55f, 44.74f, 147.17f, 44.39f, 145.8f, 44.39f)
                horizontalLineTo(144.1f)
                verticalLineTo(48f)
                horizontalLineTo(142.93f)
                verticalLineTo(39.24f)
                horizontalLineTo(145.73f)
                curveTo(146.2f, 39.24f, 146.58f, 39.25f, 147f, 39.3f)
                curveTo(148.29f, 39.44f, 149.2f, 40.13f, 149.2f, 41.65f)
                curveTo(149.2f, 42.74f, 148.6f, 43.5f, 147.64f, 43.84f)
                curveTo(148.36f, 44.11f, 148.76f, 44.68f, 148.87f, 45.64f)
                curveTo(148.97f, 46.51f, 149.07f, 47.03f, 149.22f, 47.66f)
                lineTo(149.43f, 47.93f)
                close()
                moveTo(144.1f, 43.36f)
                horizontalLineTo(145.86f)
                curveTo(147.09f, 43.36f, 148.02f, 43.06f, 148.02f, 41.82f)
                curveTo(148.02f, 40.73f, 147.4f, 40.27f, 145.88f, 40.27f)
                horizontalLineTo(144.1f)
                verticalLineTo(43.36f)
                close()
                moveTo(156.46f, 39.24f)
                verticalLineTo(40.27f)
                horizontalLineTo(151.85f)
                verticalLineTo(43.07f)
                horizontalLineTo(156.21f)
                verticalLineTo(44.1f)
                horizontalLineTo(151.85f)
                verticalLineTo(46.97f)
                horizontalLineTo(156.57f)
                verticalLineTo(48f)
                horizontalLineTo(150.7f)
                verticalLineTo(39.24f)
                horizontalLineTo(156.46f)
                close()
                moveTo(166.18f, 39.24f)
                verticalLineTo(48f)
                horizontalLineTo(165.02f)
                verticalLineTo(43.21f)
                curveTo(165.02f, 42.47f, 165.01f, 41.59f, 165.02f, 40.79f)
                curveTo(164.76f, 41.59f, 164.48f, 42.47f, 164.21f, 43.21f)
                lineTo(162.46f, 48f)
                horizontalLineTo(161.44f)
                lineTo(159.71f, 43.21f)
                curveTo(159.45f, 42.49f, 159.17f, 41.64f, 158.92f, 40.84f)
                curveTo(158.93f, 41.63f, 158.93f, 42.48f, 158.93f, 43.21f)
                verticalLineTo(48f)
                horizontalLineTo(157.78f)
                verticalLineTo(39.24f)
                horizontalLineTo(159.39f)
                curveTo(160.47f, 42.19f, 161.18f, 44.16f, 161.98f, 46.43f)
                curveTo(162.8f, 44.16f, 163.53f, 42.19f, 164.61f, 39.24f)
                horizontalLineTo(166.18f)
                close()
                moveTo(168.93f, 39.24f)
                verticalLineTo(48f)
                horizontalLineTo(167.78f)
                verticalLineTo(39.24f)
                horizontalLineTo(168.93f)
                close()
                moveTo(177.12f, 44.6f)
                curveTo(177.12f, 44.88f, 177.11f, 45.13f, 177.07f, 45.55f)
                curveTo(176.97f, 47.08f, 176.1f, 48.17f, 173.81f, 48.17f)
                curveTo(171.51f, 48.17f, 170.61f, 47.08f, 170.51f, 45.55f)
                curveTo(170.48f, 45.14f, 170.48f, 44.88f, 170.48f, 44.6f)
                verticalLineTo(39.24f)
                horizontalLineTo(171.65f)
                verticalLineTo(44.94f)
                curveTo(171.65f, 46.22f, 172.11f, 47.11f, 173.81f, 47.11f)
                curveTo(175.51f, 47.11f, 175.98f, 46.21f, 175.98f, 44.94f)
                verticalLineTo(39.24f)
                horizontalLineTo(177.12f)
                verticalLineTo(44.6f)
                close()
                moveTo(187.06f, 39.24f)
                verticalLineTo(48f)
                horizontalLineTo(185.9f)
                verticalLineTo(43.21f)
                curveTo(185.9f, 42.47f, 185.88f, 41.59f, 185.9f, 40.79f)
                curveTo(185.64f, 41.59f, 185.35f, 42.47f, 185.09f, 43.21f)
                lineTo(183.34f, 48f)
                horizontalLineTo(182.32f)
                lineTo(180.59f, 43.21f)
                curveTo(180.33f, 42.49f, 180.05f, 41.64f, 179.8f, 40.84f)
                curveTo(179.81f, 41.63f, 179.81f, 42.48f, 179.81f, 43.21f)
                verticalLineTo(48f)
                horizontalLineTo(178.66f)
                verticalLineTo(39.24f)
                horizontalLineTo(180.27f)
                curveTo(181.35f, 42.19f, 182.05f, 44.16f, 182.86f, 46.43f)
                curveTo(183.68f, 44.16f, 184.41f, 42.19f, 185.49f, 39.24f)
                horizontalLineTo(187.06f)
                close()
            }
        }.build()

        return _PrimalPremiumDark!!
    }

@Suppress("ObjectPropertyName")
private var _PrimalPremiumDark: ImageVector? = null