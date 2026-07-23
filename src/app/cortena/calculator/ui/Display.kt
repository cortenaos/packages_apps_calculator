/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026-present The CortenaOS Project
 */
package app.cortena.calculator.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import framework.cortena.ui.components.Text
import framework.cortena.ui.components.TextRole
import framework.cortena.ui.theme.LocalColors
import framework.cortena.ui.theme.LocalSpacing

/**
 * Calculator display panel — the top portion of the screen showing the expression history
 * (secondary) and the current value (primary).
 *
 * ## CortenaUI Validation Points
 * - **Typography hierarchy**: Large font for the primary result, [TextRole.TitleLarge] for the
 *   expression — validates the semantic scale gap between display-tier and body-tier roles.
 * - **Color resolution**: Uses [LocalColors] semantic roles (`onBackground` for primary,
 *   `onSurfaceVariant` for secondary) — validates adaptive theming.
 * - **Spacing tokens**: Reads [LocalSpacing] for consistent padding — validates the 4dp grid
 *   system.
 * - **Dynamic font sizing**: The primary display text auto-shrinks when it would overflow the
 *   available width, ensuring long numbers (with a thousand separators) remain fully visible.
 *
 * Layout:
 * ```
 * ┌────────────────────────────┐
 * │                            │
 * │               (flex space) │
 * │                            │
 * │          12 + 34   ← expr  │
 * │              46   ← value  │
 * └────────────────────────────┘
 * ```
 *
 * Both lines are right-aligned and pinned to the bottom of the flex area.
 */
@Composable
fun Display(expression: String, display: String, modifier: Modifier = Modifier) {
    val colors = LocalColors.current
    val spacing = LocalSpacing.current

    Box(
        modifier =
            modifier.fillMaxSize().padding(horizontal = spacing.Md.dp, vertical = spacing.Sm.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            // Expression line (secondary)
            if (expression.isNotEmpty()) {
                Text(
                    text = expression,
                    role = TextRole.TitleLarge,
                    style = TextStyle(fontWeight = FontWeight(400)),
                    color = Color(colors.onSurfaceVariant),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = spacing.Xs.dp),
                )
            }

            // Primary display (result / current input) — auto-shrinks to fit.
            AutoSizeDisplay(text = display, color = Color(colors.onBackground))
        }
    }
}

/**
 * Auto-sizing display text. Measures the text at decreasing font sizes until it fits within the
 * available width, then renders with CortenaUI's [Text].
 */
@Composable
private fun AutoSizeDisplay(text: String, color: Color, modifier: Modifier = Modifier) {
    val maxFontSizeSp = 76.04f // Calculated as: DisplaySmall * (categoryScaleRatio ^ 2)
    val minFontSizeSp = 24f
    val stepFactor = 0.9f

    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxWidthPx = constraints.maxWidth

        // Find the largest font size that fits within the available width.
        var fontSize = maxFontSizeSp
        while (fontSize > minFontSizeSp) {
            val style = TextStyle(fontSize = fontSize.sp, fontWeight = FontWeight(400))
            val measured = textMeasurer.measure(text = text, style = style, maxLines = 1)
            if (measured.size.width <= maxWidthPx) break
            fontSize *= stepFactor
        }
        fontSize = fontSize.coerceAtLeast(minFontSizeSp)

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = text,
                style = TextStyle(fontWeight = FontWeight(400), fontSize = fontSize.sp),
                color = color,
                maxLines = 1,
            )
        }
    }
}
