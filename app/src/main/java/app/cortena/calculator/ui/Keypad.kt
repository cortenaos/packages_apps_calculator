/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026-present The CortenaOS Project
 */
package app.cortena.calculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import framework.cortena.ui.components.Button
import framework.cortena.ui.components.ButtonStyle
import framework.cortena.ui.components.ButtonVariant
import framework.cortena.ui.components.Icon
import framework.cortena.ui.components.Text
import framework.cortena.ui.components.TextRole
import framework.cortena.ui.size.SizeToken
import framework.cortena.ui.theme.LocalSpacing

// TODO: CortenaUI Framework Gap — Grid Layout Abstraction
//  The 4-column calculator keypad is built with nested Row/Column + weight,
//  which is the standard Compose primitive. A CortenaUI `Grid` composable
//  with built-in spacing token integration would reduce boilerplate for
//  grid-based layouts (calculators, keyboards, settings grids, icon grids).

// TODO: CortenaUI Framework Gap — Button.shape Parameter
//  Calculator keys would ideally use RoundedShape(12.dp) instead of the
//  default CapsuleShape. Currently Button's shape is locked. Adding an
//  optional `shape: ComponentShape = CapsuleShape()` parameter would let
//  consumers adapt Button for grid-based layouts without losing the press
//  physics, highlight, and content scaling. For now, the capsule shape
//  actually works well for the minimalist aesthetic we're targeting.

// TODO: CortenaUI Framework Gap — Haptic Feedback API
//  Calculator keys should produce light haptic feedback on press.
//  CortenaUI's Button has DampedAnimation for visual feedback but no
//  framework-level haptic coordination. A `LocalHaptics` CompositionLocal
//  or a `hapticFeedback` parameter on interactive components would let
//  the OS enforce consistent tactile language across all apps.

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
    onClearEntry: () -> Unit,
    onBackspace: () -> Unit,
    onDecimal: () -> Unit,
    onPercent: () -> Unit,
    onNegate: () -> Unit,
    hasInput: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val gap = spacing.Sm.dp

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        // Row 1: ⌫, AC/C, %, ÷
        KeyRow(gap) {
            Button(
                onClick = onBackspace,
                iconOnly = true,
                style = ButtonStyle.Ghost,
                modifier = Modifier.weight(1f).aspectRatio(1f),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                )
            }
            UtilityKey(
                label = if (hasInput) "C" else "AC",
                onClick = if (hasInput) onClearEntry else onClear,
                modifier = Modifier.weight(1f),
            )
            UtilityKey(label = "%", onClick = onPercent, modifier = Modifier.weight(1f))
            OperatorKey(label = "÷", onClick = { onOperator('÷') }, modifier = Modifier.weight(1f))
        }

        // Row 2: 7, 8, 9, ×
        KeyRow(gap) {
            NumberKey(digit = '7', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = '8', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = '9', onClick = onDigit, modifier = Modifier.weight(1f))
            OperatorKey(label = "×", onClick = { onOperator('×') }, modifier = Modifier.weight(1f))
        }

        // Row 3: 4, 5, 6, −
        KeyRow(gap) {
            NumberKey(digit = '4', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = '5', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = '6', onClick = onDigit, modifier = Modifier.weight(1f))
            OperatorKey(label = "−", onClick = { onOperator('-') }, modifier = Modifier.weight(1f))
        }

        // Row 4: 1, 2, 3, +
        KeyRow(gap) {
            NumberKey(digit = '1', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = '2', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = '3', onClick = onDigit, modifier = Modifier.weight(1f))
            OperatorKey(label = "+", onClick = { onOperator('+') }, modifier = Modifier.weight(1f))
        }

        // Row 5: +/-, 0, comma, =
        KeyRow(gap) {
            Button(
                onClick = onNegate,
                style = ButtonStyle.Secondary,
                variant = ButtonVariant.Soft,
                modifier = Modifier.weight(1f).aspectRatio(1f),
            ) {
                Text(
                    text = "+/−",
                    role = TextRole.HeadlineLarge,
                    style = TextStyle(fontWeight = FontWeight(400)),
                )
            }
            NumberKey(digit = '0', onClick = onDigit, modifier = Modifier.weight(1f))
            NumberKey(digit = ',', onClick = { onDecimal() }, modifier = Modifier.weight(1f))
            OperatorKey(label = "=", onClick = { onEquals() }, modifier = Modifier.weight(1f))
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
        modifier = modifier.aspectRatio(1f),
    ) {
        Text(
            text = digit.toString(),
            role = TextRole.DisplayMedium,
            style = TextStyle(fontWeight = FontWeight(400)),
        )
    }
}

/** Operator key: Accent style — visually prominent, draws the eye. */
@Composable
private fun OperatorKey(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, style = ButtonStyle.Accent, modifier = modifier.aspectRatio(1f)) {
        Text(
            text = label,
            role = TextRole.DisplayMedium,
            style = TextStyle(fontWeight = FontWeight(400)),
        )
    }
}

/** Utility key (AC, %): Secondary Soft — present but not dominant. */
@Composable
private fun UtilityKey(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, style = ButtonStyle.Ghost, modifier = modifier.aspectRatio(1f)) {
        Text(
            text = label,
            role = TextRole.DisplayMedium,
            style = TextStyle(fontWeight = FontWeight(400)),
        )
    }
}
