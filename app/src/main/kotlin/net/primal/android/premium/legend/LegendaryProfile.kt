package net.primal.android.premium.legend

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class LegendaryProfile(
    val id: String,
    val brush: Brush,
) {
    NO_CUSTOMIZATION(
        id = "",
        brush = Brush.linearGradient(
            listOf(Color.Transparent, Color.Transparent),
        ),
    ),
    GOLD(
        id = "GOLD",
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFFFB700),
                0.49f to Color(0xFFFFB700),
                0.50f to Color(0xFFCB721E),
                1.00f to Color(0xFFFFB700),
            ),
        ),
    ),
    AQUA(
        id = "AQUA",
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF6BCCFF),
                0.49f to Color(0xFF6BCCFF),
                0.50f to Color(0xFF247FFF),
                1.00f to Color(0xFF6BCCFF),
            ),
        ),
    ),
    SILVER(
        id = "SILVER",
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFCCCCCC),
                0.49f to Color(0xFFCCCCCC),
                0.50f to Color(0xFF777777),
                1.00f to Color(0xFFCCCCCC),
            ),
        ),
    ),
    PURPLE(
        id = "PURPLE",
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFB300D3),
                Color(0xFF4800FF),
            ),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
    ),
    PURPLE_HAZE(
        id = "PURPLEHAZE",
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFB00C4),
                Color(0xFF04F7FC),
            ),
            start = Offset.Zero,
            end = Offset.Infinite,
        ),
    ),
    TEAL(
        id = "TEAL",
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF40FCFF),
                0.20f to Color(0xFF40FCFF),
                0.75f to Color(0xFF007D9F),
                1.00f to Color(0xFF007D9F),
            ),
        ),
    ),
    BROWN(
        id = "BROWN",
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFBB9971),
                0.20f to Color(0xFFBB9971),
                0.75f to Color(0xFF5C3B22),
                1.00f to Color(0xFF5C3B22),
            ),
        ),
    ),
    BLUE(
        id = "BLUE",
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF01E0FF),
                0.05f to Color(0xFF01E0FF),
                0.30f to Color(0xFF0190F8),
                0.60f to Color(0xFF2555EE),
                0.90f to Color(0xFF4A05CA),
                1.00f to Color(0xFF4A05CA),
            ),
        ),
    ),
    SUN_FIRE(
        id = "SUNFIRE",
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFFFFA722),
                0.05f to Color(0xFFFFA722),
                0.30f to Color(0xFFFA3C3C),
                0.60f to Color(0xFFF00492),
                0.90f to Color(0xFF620BA9),
                1.00f to Color(0xFF620BA9),
            ),
        ),
    ),
    ;

    companion object {
        fun valueById(id: String): LegendaryProfile? {
            return LegendaryProfile.entries.find { it.id == id }
        }
    }
}
