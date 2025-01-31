package net.primal.android.premium.legend.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Suppress("MagicNumber")
enum class LegendaryStyle(
    val id: String,
    val color: Color,
    val primaryBrush: Brush,
    val secondaryBrush: Brush,
) {
    NO_CUSTOMIZATION(
        id = "",
        color = Color.Transparent,
        primaryBrush = Brush.linearGradient(
            listOf(Color.Transparent, Color.Transparent),
        ),
        secondaryBrush = Brush.linearGradient(
            listOf(Color.Transparent, Color.Transparent),
        ),
    ),
    GOLD(
        id = "GOLD",
        color = Color(0xFFFFB701),
        primaryBrush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFFFB700),
                0.49f to Color(0xFFFFB700),
                0.50f to Color(0xFFCB721E),
                1.00f to Color(0xFFFFAA00),
            ),
        ),
        secondaryBrush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFFFB700),
                1.00f to Color(0xFFCB721E),
            ),
        ),
    ),
    AQUA(
        id = "AQUA",
        color = Color(0xFF6BCCFF),
        primaryBrush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF6BCCFF),
                0.49f to Color(0xFF6BCCFF),
                0.50f to Color(0xFF247FFF),
                1.00f to Color(0xFF6BCCFF),
            ),
        ),
        secondaryBrush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF6BCCFF),
                1.00f to Color(0xFF247FFF),
            ),
        ),
    ),
    SILVER(
        id = "SILVER",
        color = Color(0xFFCCCCCC),
        primaryBrush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFCCCCCC),
                0.49f to Color(0xFFCCCCCC),
                0.50f to Color(0xFF777777),
                1.00f to Color(0xFFCCCCCC),
            ),
        ),
        secondaryBrush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFCCCCCC),
                1.00f to Color(0xFF777777),
            ),
        ),
    ),
    PURPLE(
        id = "PURPLE",
        color = Color(0xFFC803EC),
        primaryBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFB300D3),
                Color(0xFF4800FF),
            ),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
        secondaryBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFC803EC),
                Color(0xFF5613FF),
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 900f),
        ),
    ),
    PURPLE_HAZE(
        id = "PURPLEHAZE",
        color = Color(0xFFE812C8),
        primaryBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFB00C4),
                Color(0xFF04F7FC),
            ),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
        secondaryBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFB00C4),
                Color(0xFF04F7FC),
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 900f),
        ),
    ),
    TEAL(
        id = "TEAL",
        color = Color(0xFF40FCFF),
        primaryBrush = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF40FCFF),
                0.20f to Color(0xFF40FCFF),
                0.75f to Color(0xFF007D9F),
                1.00f to Color(0xFF007D9F),
            ),
        ),
        secondaryBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF40FCFF),
                Color(0xFF007D9F),
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 900f),
        ),
    ),
    BROWN(
        id = "BROWN",
        color = Color(0xFFBB9971),
        primaryBrush = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFBB9971),
                0.20f to Color(0xFFBB9971),
                0.75f to Color(0xFF5C3B22),
                1.00f to Color(0xFF5C3B22),
            ),
        ),
        secondaryBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFBB9971),
                Color(0xFF5C3B22),
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 900f),
        ),
    ),
    BLUE(
        id = "BLUE",
        color = Color(0xFF2394EF),
        primaryBrush = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF01E0FF),
                0.05f to Color(0xFF01E0FF),
                0.35f to Color(0xFF0190F8),
                0.75f to Color(0xFF2555EE),
                1.00f to Color(0xFF2555EE),
            ),
        ),
        secondaryBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF01E0FF),
                Color(0xFF0190F8),
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 900f),
        ),
    ),
    SUN_FIRE(
        id = "SUNFIRE",
        color = Color(0xFFCA077C),
        primaryBrush = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFFFA722),
                0.05f to Color(0xFFFFA722),
                0.35f to Color(0xFFFA3C3C),
                0.75f to Color(0xFFF00492),
                1.00f to Color(0xFFF00492),
            ),
        ),
        secondaryBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFA722),
                Color(0xFFFA3C3C),
                Color(0xFFF00492),
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 900f),
        ),
    ),
    ;

    companion object {
        fun valueById(id: String?): LegendaryStyle? {
            return LegendaryStyle.entries.find { it.id == id }
        }
    }
}
