/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026-present The CortenaOS Project
 */
package app.cortena.calculator.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cortena.calculator.engine.Engine
import framework.cortena.ui.theme.LocalSpacing

// TODO: CortenaUI Framework Gap — Surface / Card Component
//  The display area and keypad area are logically separate "surfaces".
//  These would ideally be distinct visual panels with subtle
//  background differentiation (e.g. display on background, keypad on
//  surface or surfaceContainer). CortenaUI currently has no Surface/Card
//  component that provides:
//    - shaped container (background + clip)
//    - content color propagation (LocalContentColor)
//    - optional elevation / shadow
//  For now, both areas sit on the shared Body background. A framework
//  Surface component would let us visually separate them without ad-hoc
//  Modifier stacking.

/**
 * Root calculator screen — composes [Display] and [Keypad] into a vertical split layout.
 *
 * ## CortenaUI Validation Points
 * - **Layout stack**: `Column` filling the full `Body` area with the display taking remaining space
 *   (`weight(1f)`) and the keypad at natural height. This validates the flex-layout pattern for
 *   fixed-viewport apps.
 * - **Spacing tokens**: Bottom padding from [LocalSpacing] ensures the keypad doesn't touch the
 *   navigation bar area.
 * - **No ScrollView**: Calculator is fixed-viewport — validates that CortenaUI's layout primitives
 *   work correctly *without* ScrollView wrapping.
 *
 * @param engine The [Engine] driving state and callbacks.
 */
@Composable
fun Screen(engine: Engine) {
    val spacing = LocalSpacing.current
    val state = engine.state

    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = spacing.Sm.dp)
    ) {
        // Display area (flex: takes remaining vertical space)
        Display(
            expression = state.expression,
            display = state.display,
            modifier = Modifier.weight(1f),
        )

        // Keypad area (fixed: natural height from button grid)
        Keypad(
            onDigit = engine::onDigit,
            onOperator = engine::onOperator,
            onEquals = engine::onEquals,
            onClear = engine::onClear,
            onBackspace = engine::onBackspace,
            onDecimal = engine::onDecimal,
            onPercent = engine::onPercent,
            modifier = Modifier.padding(horizontal = spacing.Sm.dp),
        )
    }
}
