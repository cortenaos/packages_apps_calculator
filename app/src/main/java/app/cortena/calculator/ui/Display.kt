/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026-present The CortenaOS Project
 */
package app.cortena.calculator.ui

import androidx.compose.foundation.layout.Box
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
 * - **Typography hierarchy**: [TextRole.DisplayLarge] for the primary result, [TextRole.BodyLarge]
 *   for the expression — validates the semantic scale gap between display-tier and body-tier roles.
 * - **Color resolution**: Uses [LocalColors] semantic roles (`onBackground` for primary,
 *   `onSurfaceVariant` for secondary) — validates adaptive theming.
 * - **Spacing tokens**: Reads [LocalSpacing] for consistent padding — validates the 4dp grid
 *   system.
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

    // Box takes all remaining vertical space (via weight in the parent Column)
    // and aligns content to bottom-end — the natural reading position.
    Box(
        modifier =
            modifier.fillMaxSize().padding(horizontal = spacing.Md.dp, vertical = spacing.Sm.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            // Expression line (secondary)
            // Only shown when there is an active expression.
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

            // Primary display (result / current input)
            Text(
                text = display,
                style =
                    TextStyle(
                        fontWeight = FontWeight(400),
                        fontSize = 67.38.sp,
                    ), // DisplayLarge x 1.437
                color = Color(colors.onBackground),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
