/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (C) 2026-present The CortenaOS Project
 */
package app.cortena.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import app.cortena.calculator.engine.Engine
import app.cortena.calculator.ui.Screen
import framework.cortena.ui.layout.Body
import framework.cortena.ui.layout.ContentView

/**
 * Entry point for the CortenaOS Calculator.
 *
 * Follows the documented CortenaUI pattern: `ContentView` → `Body` → screen content.
 * - [ContentView] provides edge-to-edge, theme injection, and status bar management.
 * - [Body] fills the remaining space with the background color.
 * - [Screen] composes the display + keypad layout.
 *
 * The [Engine] is remembered at the Activity scope so it survives recompositions but not
 * configuration changes. For a framework stress-test this is sufficient; production would use a
 * ViewModel.
 */
class CalculatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContentView {
            Body {
                val engine = remember { Engine() }
                Screen(engine = engine)
            }
        }
    }
}
