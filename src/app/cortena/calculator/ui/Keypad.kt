/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026-present The CortenaOS Project
 */
package app.cortena.calculator.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import framework.cortena.icons.PhosphorIcon
import framework.cortena.icons.PhosphorIcons
import framework.cortena.ui.components.Button
import framework.cortena.ui.components.ButtonStyle
import framework.cortena.ui.components.ButtonVariant
import framework.cortena.ui.components.Icon
import framework.cortena.ui.components.Text
import framework.cortena.ui.components.TextRole
import framework.cortena.ui.size.SizeToken
import framework.cortena.ui.theme.LocalColors
import framework.cortena.ui.theme.LocalSpacing

// TODO: CortenaUI Framework Gap — Grid Layout Abstraction
//  The 4-column calculator keypad is built with nested Row/Column + weight,
//  which is the standard Compose primitive. A CortenaUI `Grid` composable
//  with built-in spacing token integration would reduce boilerplate for
//  grid-based layouts (calculators, keyboards, settings grids, icon grids).

/**
 * Calculator button grid — the bottom portion of the screen.
 *
 * ## CortenaUI Validation Points
 * - **Button styles**: Validates all relevant [ButtonStyle] variants in a dense layout —
 *   [ButtonStyle.Ghost] (number keys), [ButtonStyle.Accent] (operators),
 *   [ButtonStyle.Secondary] + [ButtonVariant.Soft] (utility keys), [ButtonStyle.Primary] (equals).
 * - **Button sizing**: Uses [SizeToken.Large] for comfortable touch targets in a grid context —
 *   validates that the size tier system produces appropriate proportions for non-list button
 *   layouts.
 * - **Spacing tokens**: [LocalSpacing] `Sm` (8dp) for both horizontal and vertical gaps — validates
 *   that the 4dp grid produces comfortable grid spacing.
 * - **Icon inside Button**: The backspace key places a Material [Icon] inside [Button] — validates
 *   the content-scaling contract (icon auto-sizes to the button's tier).
 * - **Dynamic label**: AC/C toggles based on [hasInput] — validates that stateful label changes
 *   integrate cleanly with CortenaUI's Button recomposition.
 *
 * Layout:
 * ```
 * ┌──────┬──────┬──────┬──────┐
 * │  ⌫   │  AC  │   %  │   ÷  │
 * ├──────┼──────┼──────┼──────┤
 * │  7   │  8   │   9  │   ×  │
 * ├──────┼──────┼──────┼──────┤
 * │  4   │  5   │   6  │   −  │
 * ├──────┼──────┼──────┼──────┤
 * │  1   │  2   │   3  │   +  │
 * ├──────┼──────┼──────┼──────┤
 * │ +/-  │  0   │   ,  │   =  │
 * └──────┴──────┴──────┴──────┘
 * ```
 */
@Composable
fun Keypad(
    onDigit: (Char) -> Unit,
    onOperator: (Char) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onDecimal: () -> Unit,
    onPercent: () -> Unit,
    onNegate: () -> Unit,
    hasInput: Boolean,
    activeOperator: Char?,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val gap = spacing.Sm.dp

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        // Row 1: ⌫, AC, %, ÷
        KeyRow(gap) {
            Button(
                onClick = onBackspace,
                iconOnly = true,
                style = ButtonStyle.Ghost,
                modifier = Modifier.weight(1f).aspectRatio(1f),
            ) {
                Icon(
                    renderer = PhosphorIcon(PhosphorIcons.Regular.Backspace),
                    contentDescription = "Backspace",
                    size = 48.dp,
                )
            }
            UtilityKey(
                label = "AC",
                onClick = onClear,
                modifier = Modifier.weight(1f),
            )
            UtilityKey(
                icon = {
                    Icon(
                        renderer = PhosphorIcon(PhosphorIcons.Regular.Percent),
                        contentDescription = "Percent",
                        size = 48.dp,
                    )
                },
                onClick = onPercent,
                modifier = Modifier.weight(1f)
            )
            OperatorKey(
                icon = {
                    Icon(
                        renderer = PhosphorIcon(PhosphorIcons.Regular.Divide),
                        contentDescription = "Divide",
                        size = 48.dp,
                    )
                },
                onClick = { onOperator('÷') },
                isActive = activeOperator == '÷',
                modifier = Modifier.weight(1f),
            )
        }

        // Row 2: 7, 8, 9, ×
        KeyRow(gap) {
            NumberKey(digit = '7', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = '8', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = '9', onClick = onDigit, modifier = Modifier.weight(1f))
            OperatorKey(
                icon = {
                    Icon(
                        renderer = PhosphorIcon(PhosphorIcons.Regular.X),
                        contentDescription = "Multiply",
                        size = 48.dp,
                    )
                },
                onClick = { onOperator('×') },
                isActive = activeOperator == '×',
                modifier = Modifier.weight(1f),
            )
        }

        // Row 3: 4, 5, 6, -
        KeyRow(gap) {
            NumberKey(digit = '4', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = '5', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = '6', onClick = onDigit, modifier = Modifier.weight(1f))
            OperatorKey(
                icon = {
                    Icon(
                        renderer = PhosphorIcon(PhosphorIcons.Regular.Minus),
                        contentDescription = "Minus",
                        size = 48.dp,
                    )
                },
                onClick = { onOperator('-') },
                isActive = activeOperator == '-',
                modifier = Modifier.weight(1f),
            )
        }

        // Row 4: 1, 2, 3, +
        KeyRow(gap) {
            NumberKey(digit = '1', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = '2', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = '3', onClick = onDigit, modifier = Modifier.weight(1f))
            OperatorKey(
                icon = {
                    Icon(
                        renderer = PhosphorIcon(PhosphorIcons.Regular.Plus),
                        contentDescription = "Plus",
                        size = 48.dp,
                    )
                },
                onClick = { onOperator('+') },
                isActive = activeOperator == '+',
                modifier = Modifier.weight(1f),
            )
        }

        // Row 5: +/-, 0, comma, =
        KeyRow(gap) {
            Button(
                onClick = onNegate,
                iconOnly = true,
                style = ButtonStyle.Secondary,
                variant = ButtonVariant.Soft,
                modifier = Modifier.weight(1f).aspectRatio(1f),
            ) {
                Icon(
                    renderer = PhosphorIcon(PhosphorIcons.Regular.PlusMinus),
                    contentDescription = "Plus Minus",
                    size = 48.dp,
                )
            }
            NumberKey(digit = '0', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = ',', onClick = { onDecimal() }, modifier = Modifier.weight(1f))
            OperatorKey(
                icon = {
                    Icon(
                        renderer = PhosphorIcon(PhosphorIcons.Regular.Equals),
                        contentDescription = "Equals",
                        size = 48.dp,
                    )
                },
                onClick = { onEquals() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Row container with consistent horizontal spacing. Each row gets `aspectRatio` constraints from
 * the individual keys.
 */
@Composable
private fun KeyRow(gap: Dp, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap),
        content = content,
    )
}

/** Number key: Ghost style — subdued background, lets the digit stand out. */
@Composable
private fun NumberKey(digit: Char, onClick: (Char) -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = { onClick(digit) },
        style = ButtonStyle.Secondary,
        variant = ButtonVariant.Soft,
        iconOnly = true,
        modifier = modifier.aspectRatio(1f),
    ) {
        Text(
            text = digit.toString(),
            role = TextRole.DisplayMedium,
            style = TextStyle(fontWeight = FontWeight(400)),
        )
    }
}

/** Operator key: Accent style — visually prominent, draws the eye. Swaps fg/bg when active. */
@Composable
private fun OperatorKey(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
) {
    val colors = LocalColors.current
    Button(
        onClick = onClick,
        style = ButtonStyle.Accent,
        // When active: white bg + accent(orange) fg. Normal: uses default Accent colors.
        background = if (isActive) Color.White else Color.Unspecified,
        foreground = if (isActive) Color(colors.accent) else Color.Unspecified,
        iconOnly = true,
        modifier = modifier.aspectRatio(1f),
    ) {
        icon()
    }
}

/** Utility key (AC, %): Secondary Soft — present but not dominant. */
@SuppressLint("ModifierParameter")
@Composable
private fun UtilityKey(
    label: String? = null,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        style = ButtonStyle.Ghost,
        iconOnly = true,
        modifier = modifier.aspectRatio(1f),
    ) {
        if (icon != null) {
            icon()
        } else if (label != null) {
            Text(
                text = label,
                role = TextRole.DisplayMedium,
                style = TextStyle(fontWeight = FontWeight(400)),
            )
        }
    }
}
